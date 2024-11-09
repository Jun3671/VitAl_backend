package VitAI.injevital.controller;

import VitAI.injevital.dto.*;
import VitAI.injevital.entity.Member;
import VitAI.injevital.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@RestController
@RequestMapping("/bot")
public class DietAIController {
    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiURL;

    @Autowired
    private RestTemplate template;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/diet-recommendation")
    public ResponseEntity<DietRecommendationResponse> getDietRecommendation(
            @RequestBody DietRecommendationRequest request) {

        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // BMI 및 일일 칼로리 계산
        double height = Double.parseDouble(String.valueOf(member.getMemberHeight()));
        double weight = Double.parseDouble(String.valueOf(member.getMemberWeight()));
        double bmi = calculateBMI(height, weight);
        double dailyCalories = calculateDailyCalories(weight, height, request.getGoal());


        String prompt = String.format(
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
                height, weight, bmi, dailyCalories, request.getGoal(), request.getFoodType()
        );

        ChatGPTRequest gptRequest = new ChatGPTRequest(model, prompt);
        ChatGPTResponse chatGPTResponse = template.postForObject(apiURL, gptRequest, ChatGPTResponse.class);

        if (chatGPTResponse != null && !chatGPTResponse.getChoices().isEmpty()) {
            try {
                String jsonResponse = chatGPTResponse.getChoices().get(0).getMessage().getContent();


                // 백틱(```) 제거
                jsonResponse = jsonResponse.replace("```json", "").replace("```", "").trim();
                System.out.println("ChatGPT JSON Response (cleaned): " + jsonResponse);
                // ChatGPT 응답을 파싱하여 구조화된 데이터로 변환

                // JSON 파싱 시작
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
                NutritionInfoDto dailySummary = new NutritionInfoDto();
                response.getMeals().forEach(meal -> {
                    dailySummary.setCalories(dailySummary.getCalories() + meal.getNutrition().getCalories());
                    dailySummary.setProtein(dailySummary.getProtein() + meal.getNutrition().getProtein());
                    dailySummary.setCarbs(dailySummary.getCarbs() + meal.getNutrition().getCarbs());
                    dailySummary.setFat(dailySummary.getFat() + meal.getNutrition().getFat());
                });
                response.setDailyNutritionSummary(dailySummary);

                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().build();
            }
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    private double calculateBMI(double heightCm, double weightKg) {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    private double calculateDailyCalories(double weightKg, double heightCm, String goal) {
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
}