package VitAI.injevital.service;

import VitAI.injevital.dto.*;
import VitAI.injevital.entity.Member;
import VitAI.injevital.util.CalculationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class DietService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiURL;

    /**
     * 일일 필요 칼로리 계산
     * @param weightKg 체중 (kg)
     * @param heightCm 키 (cm)
     * @param goal 목표 (감량/유지/증량)
     * @return 일일 필요 칼로리
     */
    public double calculateDailyCalories(double weightKg, double heightCm, String goal) {
        double bmr = 66.5 + (13.75 * weightKg) + (5.003 * heightCm) - (6.75 * 25); // 나이는 25로 가정
        double activityFactor = 1.375; // 보통 활동량 가정
        double targetCalories = bmr * activityFactor;

        switch (goal.toLowerCase()) {
            case "감량":
                targetCalories *= 0.8;
                break;
            case "증량":
                targetCalories *= 1.2;
                break;
        }
        return targetCalories;
    }

    /**
     * ChatGPT API를 사용하여 식단 추천 생성
     * @param member 회원 정보
     * @param request 식단 추천 요청 정보
     * @return 식단 추천 응답
     * @throws Exception 파싱 실패 시
     */
    public DietRecommendationResponse generateDietRecommendation(Member member, DietRecommendationRequest request) throws Exception {
        double height = Double.parseDouble(String.valueOf(member.getMemberHeight()));
        double weight = Double.parseDouble(String.valueOf(member.getMemberWeight()));
        double bmi = CalculationUtils.calculateBmi(height, weight);
        double dailyCalories = calculateDailyCalories(weight, height, request.getGoal());

        String prompt = buildDietPrompt(height, weight, bmi, dailyCalories, request.getGoal(), request.getFoodType());

        ChatGPTRequest gptRequest = new ChatGPTRequest(model, prompt);
        ChatGPTResponse chatGPTResponse = restTemplate.postForObject(apiURL, gptRequest, ChatGPTResponse.class);

        if (chatGPTResponse == null || chatGPTResponse.getChoices().isEmpty()) {
            throw new Exception("ChatGPT API 응답이 비어있습니다.");
        }

        String jsonResponse = chatGPTResponse.getChoices().get(0).getMessage().getContent();
        jsonResponse = jsonResponse.replace("```json", "").replace("```", "").trim();

        return parseDietRecommendation(jsonResponse, height, weight, bmi, dailyCalories, request);
    }

    /**
     * 식단 추천 프롬프트 생성
     */
    private String buildDietPrompt(double height, double weight, double bmi, double dailyCalories, String goal, String foodType) {
        return String.format(
                """
                다음 조건에 맞는 상세한 식단을 JSON 형식으로 제공해주세요:

                조건:
                - 키: %.1fcm
                - 몸무게: %.1fkg
                - BMI: %.2f
                - 일일 필요 칼로리: %.0f kcal
                - 목표: %s
                - 선호 음식: %s

                응답 형식:
                {
                    "meals": [
                        {
                            "mealType": "아침/점심/저녁",
                            "mainMenu": ["메뉴1", "메뉴2", "메뉴3"],
                            "description": "식단 설명",
                            "nutrition": {
                                "calories": 숫자,
                                "protein": 숫자,
                                "carbs": 숫자,
                                "fat": 숫자
                            }
                        }
                    ],
                    "healthAdvice": [
                        "조언1",
                        "조언2",
                        "조언3"
                    ]
                }
                """,
                height, weight, bmi, dailyCalories, goal, foodType
        );
    }

    /**
     * ChatGPT 응답을 파싱하여 DietRecommendationResponse 생성
     */
    private DietRecommendationResponse parseDietRecommendation(
            String jsonResponse,
            double height,
            double weight,
            double bmi,
            double dailyCalories,
            DietRecommendationRequest request) throws Exception {

        var gptData = objectMapper.readTree(jsonResponse);

        // 사용자 정보 설정
        UserInfo userInfo = new UserInfo();
        userInfo.setHeight(height);
        userInfo.setWeight(weight);
        userInfo.setBmi(bmi);
        userInfo.setDailyCalorieNeeds(dailyCalories);
        userInfo.setGoal(request.getGoal());
        userInfo.setPreferredFoodType(request.getFoodType());

        DietRecommendationResponse response = new DietRecommendationResponse();
        response.setUserInfo(userInfo);

        // 식사 정보 파싱
        response.setMeals(Arrays.asList(
                objectMapper.treeToValue(gptData.get("meals"), MealPlanDto[].class)
        ));

        // 건강 조언 파싱
        response.setHealthAdvice(Arrays.asList(
                objectMapper.treeToValue(gptData.get("healthAdvice"), String[].class)
        ));

        // 일일 영양 요약 계산
        NutritionInfoDto dailySummary = calculateDailyNutritionSummary(response.getMeals());
        response.setDailyNutritionSummary(dailySummary);

        return response;
    }

    /**
     * 일일 영양 요약 계산
     */
    private NutritionInfoDto calculateDailyNutritionSummary(Iterable<MealPlanDto> meals) {
        NutritionInfoDto dailySummary = new NutritionInfoDto();
        meals.forEach(meal -> {
            dailySummary.setCalories(dailySummary.getCalories() + meal.getNutrition().getCalories());
            dailySummary.setProtein(dailySummary.getProtein() + meal.getNutrition().getProtein());
            dailySummary.setCarbs(dailySummary.getCarbs() + meal.getNutrition().getCarbs());
            dailySummary.setFat(dailySummary.getFat() + meal.getNutrition().getFat());
        });
        return dailySummary;
    }
}