package VitAI.injevital.service;

import VitAI.injevital.dto.ChatGPTRequest;
import VitAI.injevital.dto.ChatGPTResponse;
import VitAI.injevital.dto.UserStats;
import VitAI.injevital.entity.Exercise;
import VitAI.injevital.entity.Member;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI와의 통신을 담당하는 서비스
 * OpenAI API를 호출하여 운동 추천을 받고, 실패 시 fallback 로직을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExerciseAIService {

    private final RestTemplate restTemplate;
    private final ExerciseCalculationService calculationService;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiURL;

    /**
     * AI API를 호출하여 운동 추천을 받습니다.
     * 실패 시 fallback 로직을 통해 기본 추천을 제공합니다.
     */
    public ChatGPTResponse getAIRecommendations(ChatGPTRequest request, String targetPart, Member member) {
        try {
            // Create headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            // Format messages
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a professional fitness trainer providing exercise recommendations.");
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", request.getMessages().get(0).getContent());
            messages.add(userMessage);

            requestBody.put("messages", messages);

            // Log request
            ObjectMapper mapper = new ObjectMapper();
            log.info("API 요청 URL: {}", apiURL);
            log.info("API 요청 본문: {}", mapper.writeValueAsString(requestBody));

            // Make request
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiURL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Log response
            log.info("API 응답: {}", response.getBody());

            // Parse response
            return mapper.readValue(response.getBody(), ChatGPTResponse.class);

        } catch (Exception e) {
            log.error("AI 서비스 호출 중 오류 발생: {}", e.getMessage());
            UserStats userStats = buildUserStats(member);
            return fallbackRecommendation(targetPart, userStats);
        }
    }

    /**
     * AI API 호출 실패 시 사용되는 기본 운동 추천 로직
     */
    private ChatGPTResponse fallbackRecommendation(String targetPart, UserStats userStats) {
        // 운동 부위별 기본 추천 운동 매핑
        Map<String, List<ExerciseTemplate>> exercisesByPart = Map.of(
                "어깨", List.of(
                        new ExerciseTemplate("덤벨 숄더 프레스", "기본적인 어깨 운동으로 전체적인 어깨 발달에 효과적입니다"),
                        new ExerciseTemplate("사이드 레터럴 레이즈", "측면 삼각근 발달에 도움이 되는 기초 운동입니다")
                ),
                "등", List.of(
                        new ExerciseTemplate("덤벨 로우", "기본적인 등 운동으로 광배근 발달에 효과적입니다"),
                        new ExerciseTemplate("랫풀다운", "등 넓이 발달에 도움이 되는 기초 운동입니다")
                ),
                "가슴", List.of(
                        new ExerciseTemplate("푸시업", "장소에 구애받지 않고 할 수 있는 기초 가슴운동입니다"),
                        new ExerciseTemplate("벤치프레스", "대표적인 가슴 운동으로 전체적인 가슴 발달에 효과적입니다")
                )
                // ... 다른 부위도 추가
        );

        // 해당 부위의 기본 운동 가져오기 (없으면 기본값 사용)
        List<ExerciseTemplate> exercises = exercisesByPart.getOrDefault(targetPart,
                List.of(new ExerciseTemplate("기본 운동", "기초체력 향상을 위한 운동입니다")));

        // BMI에 따른 운동 강도 조정
        String sets = userStats.getBmi() > 25 ? "2-3" : "3-4";
        String reps = userStats.getBmi() > 25 ? "10-12" : "12-15";
        String restTime = userStats.getBmi() > 25 ? "90초" : "60초";

        // JSON 응답 생성
        StringBuilder jsonContent = new StringBuilder();
        jsonContent.append(String.format("""
        {
            "fitnessLevel": "%s",
            "recommendations": [
        """, calculationService.calculateFitnessLevel(userStats)));

        // 운동 추천 목록 생성
        for (int i = 0; i < exercises.size(); i++) {
            ExerciseTemplate exercise = exercises.get(i);
            jsonContent.append(String.format("""
                {
                    "name": "%s",
                    "aiReasoning": "%s",
                    "sets": "%s",
                    "reps": "%s",
                    "restTime": "%s"
                }%s
            """,
                    exercise.name,
                    exercise.reasoning,
                    sets,
                    reps,
                    restTime,
                    i < exercises.size() - 1 ? "," : ""
            ));
        }

        jsonContent.append("""
            ],
            "aiAnalysis": "현재 건강 상태를 고려한 기본적인 운동 루틴입니다. 점진적으로 강도를 높여가시기 바랍니다."
        }
        """);

        // ChatGPTResponse 생성 및 반환
        ChatGPTResponse response = new ChatGPTResponse();
        ChatGPTResponse.Choice choice = new ChatGPTResponse.Choice();
        ChatGPTResponse.Message message = new ChatGPTResponse.Message();
        message.setRole("assistant");
        message.setContent(jsonContent.toString());
        choice.setMessage(message);
        response.setChoices(List.of(choice));

        return response;
    }

    /**
     * Member 엔티티로부터 UserStats를 생성합니다.
     */
    private UserStats buildUserStats(Member member) {
        return UserStats.builder()
                .height(Double.parseDouble(String.valueOf(member.getMemberHeight())))
                .weight(Double.parseDouble(String.valueOf(member.getMemberWeight())))
                .skeletalMuscleMass(Double.parseDouble(String.valueOf(member.getMemberSmm())))
                .bodyFatMass(Double.parseDouble(String.valueOf(member.getMemberBfm())))
                .bodyFatPercentage(Double.parseDouble(String.valueOf(member.getMemberBfp())))
                .bmi(Double.parseDouble(String.valueOf(member.getMemberBmi())))
                .build();
    }

    /**
     * 운동 템플릿을 위한 내부 클래스
     */
    private static class ExerciseTemplate {
        String name;
        String reasoning;

        ExerciseTemplate(String name, String reasoning) {
            this.name = name;
            this.reasoning = reasoning;
        }
    }
}
