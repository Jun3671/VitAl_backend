// VitAI.injevital.service 패키지에 추가
package VitAI.injevital.service;

import VitAI.injevital.dto.*;
import VitAI.injevital.entity.Exercise;
import VitAI.injevital.entity.Member;
import VitAI.injevital.exception.ExerciseNotFoundException;
import VitAI.injevital.exception.InvalidExerciseRequestException;
import VitAI.injevital.repository.ExerciseRepository;
import VitAI.injevital.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiURL;

    public ExerciseRecommendationResponse getExerciseRecommendations(ExerciseRecommendationRequest request) {
        try {
            // 1. 입력값 검증
            validateRequest(request);

            // 2. 회원 정보 조회
            Member member = memberRepository.findByMemberId(request.getMemberId())
                    .orElseThrow(() -> new ExerciseNotFoundException("회원을 찾을 수 없습니다."));

            // 3. 사용자의 건강 상태를 고려한 운동 목록 조회
            List<Exercise> availableExercises = findExercisesConsideringHealthConditions(
                    request.getTargetPart(),
                    request.getHealthConditions(),
                    request.getExerciseCount()
            );

            if (availableExercises.isEmpty()) {
                throw new ExerciseNotFoundException("해당 부위의 운동을 찾을 수 없습니다.");
            }

            // 4. AI에 프롬프트 전송하여 운동 추천 받기
            ChatGPTRequest chatGPTRequest = createChatGPTRequest(member, request, availableExercises);
            // 여기서 targetPart와 member도 함께 전달
            ChatGPTResponse aiResponse = getAIRecommendations(chatGPTRequest, request.getTargetPart(), member);

            // 5. AI 응답 파싱 및 응답 생성
            return parseAIResponseAndCreateRecommendation(aiResponse, member, availableExercises);

        } catch (Exception e) {
            log.error("운동 추천 중 오류 발생", e);
            throw new RuntimeException("운동 추천을 처리할 수 없습니다.", e);
        }
    }

    private ChatGPTRequest createChatGPTRequest(Member member, ExerciseRecommendationRequest request, List<Exercise> exercises) {
        String prompt = String.format("""
    다음 사용자의 신체 정보와 운동 목표를 분석하여 완전히 개인화된 맞춤형 운동 프로그램을 추천해주세요.
    
    [사용자 정보]
    - 키: %s cm
    - 체중: %s kg
    - 골격근량: %s kg
    - 체지방량: %s kg
    - 체지방률: %s%%
    - BMI: %s
    
    [운동 목표]
    - 타겟 부위: %s
    - 건강 상태/제한사항: %s
    
    [운동 강도 가이드라인]
    1. BMI 기준:
       - 18.5 미만(저체중): 낮은 강도로 시작하여 천천히 증가
       - 18.5-23(정상): 중간 강도로 시작
       - 23-25(과체중): 약간 낮은 강도로 시작하되 유산소 운동 비중 증가
       - 25 이상(비만): 낮은 강도로 시작하고 관절 부담 최소화
    
    2. 체지방률 기준:
       - 15%% 미만: 근력 운동 강도 높게, 충분한 휴식
       - 15-25%%: 균형잡힌 강도
       - 25%% 이상: 낮은 강도로 시작, 휴식 시간 짧게
    
    3. 골격근량 기준:
       - 20kg 미만: 낮은 강도, 적은 세트수
       - 20-25kg: 기본 강도
       - 25kg 이상: 높은 강도, 많은 세트수 가능
    
    [세부 운동 강도 설정]
    1. 세트 수:
       - 초보자: 2-3세트
       - 중급자: 3-4세트
       - 고급자: 4-5세트
    
    2. 반복 횟수:
       - 근력 향상: 6-8회
       - 근비대: 8-12회
       - 근지구력: 12-15회
    
    3. 휴식 시간:
       - 고강도: 90-120초
       - 중강도: 60-90초
       - 저강도: 30-60초
    
    [가능한 운동 목록]
    %s
    
    다음 운동 %d개를 추천하고, JSON 형식으로 응답해주세요.
    각 운동마다 사용자의 신체 정보를 고려하여 반드시 서로 다른 맞춤형 운동 강도를 제시해주세요:
    {
        "fitnessLevel": "상세한 체력 수준 분석",
        "recommendations": [
            {
                "name": "운동 이름",
                "sets": "추천 세트 수 (예: 3-4)",
                "reps": "추천 반복 횟수 (예: 12-15)",
                "restTime": "추천 휴식 시간 (예: 90초)",
                "aiReasoning": "이 운동과 강도를 추천하는 구체적인 이유 (사용자의 신체 정보와 연관지어 설명)"
            }
        ],
        "aiAnalysis": "전반적인 분석 및 운동 강도 설정에 대한 상세한 설명"
    }
    
    중요: 사용자의 BMI, 체지방률, 골격근량을 종합적으로 고려하여 각각 다른 맞춤형 운동 강도를 설정해주세요.
    운동 초보자나 위험군에 속하는 경우 안전을 최우선으로 고려하여 낮은 강도로 시작하는 것을 권장합니다.
    """,
                member.getMemberHeight(),
                member.getMemberWeight(),
                member.getSkeletalMuscleMass(),
                member.getBodyFatMass(),
                member.getBodyFatPercentage(),
                member.getBmi(),
                request.getTargetPart(),
                request.getHealthConditions() != null ? String.join(", ", request.getHealthConditions()) : "없음",
                exercises.stream()
                        .map(e -> String.format("- %s (설명: %s, 난이도: %s, 기본 세트: %s, 기본 반복: %s, 기본 휴식: %s)",
                                e.getName(),
                                e.getDescription(),
                                e.getDifficulty(),
                                e.getDefaultSets(),
                                e.getDefaultReps(),
                                e.getDefaultRestTime()))
                        .collect(Collectors.joining("\n")),
                request.getExerciseCount()
        );

        return new ChatGPTRequest(model, prompt);
    }

    private ChatGPTResponse getAIRecommendations(ChatGPTRequest request, String targetPart, Member member) {
        try {
            // Create headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
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
        """, calculateFitnessLevel(userStats)));

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

    // 운동 템플릿을 위한 내부 클래스
    private static class ExerciseTemplate {
        String name;
        String reasoning;

        ExerciseTemplate(String name, String reasoning) {
            this.name = name;
            this.reasoning = reasoning;
        }
    }

    private ExerciseRecommendationResponse parseAIResponseAndCreateRecommendation(
            ChatGPTResponse aiResponse,
            Member member,
            List<Exercise> availableExercises) {

        try {
            String jsonResponse = aiResponse.getChoices().get(0).getMessage().getContent();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            UserStats userStats = buildUserStats(member);
            List<ExerciseRecommendation> recommendations = createRecommendations(
                    root.get("recommendations"),
                    availableExercises,
                    userStats  // UserStats 전달
            );

            return ExerciseRecommendationResponse.builder()
                    .userStats(userStats)
                    .fitnessLevel(root.get("fitnessLevel").asText())
                    .recommendations(recommendations)
                    .aiAnalysis(root.get("aiAnalysis").asText())
                    .build();

        } catch (Exception e) {
            log.error("AI 응답 파싱 중 오류 발생", e);
            return createDefaultResponse(member, availableExercises);
        }
    }

    private ExerciseIntensity calculateCustomIntensity(UserStats userStats, Exercise exercise) {
        // 기본값 파싱
        String[] repsRange = exercise.getDefaultReps().split("-");
        int baseReps = Integer.parseInt(repsRange[0]); // 최소값 사용

        int baseRestTime = Integer.parseInt(exercise.getDefaultRestTime().replace("초", ""));

        String[] setsRange = exercise.getDefaultSets().split("-");
        int baseSets = Integer.parseInt(setsRange[0]); // 최소값 사용

        // BMI 기반 강도 조정
        double bmiAdjustment = calculateBmiAdjustment(userStats.getBmi());

        // 체지방률 기반 강도 조정
        double bfpAdjustment = calculateBfpAdjustment(userStats.getBodyFatPercentage());

        // 골격근량 기반 강도 조정
        double muscleAdjustment = calculateMuscleAdjustment(userStats.getSkeletalMuscleMass());

        // 최종 값 계산
        int adjustedSets = calculateAdjustedSets(baseSets, bmiAdjustment, exercise.getDifficulty());
        int adjustedReps = calculateAdjustedReps(baseReps, bfpAdjustment, muscleAdjustment);
        int adjustedRestTime = calculateAdjustedRestTime(baseRestTime, bmiAdjustment, muscleAdjustment);

        // 범위 형식으로 반환
        return ExerciseIntensity.builder()
                .sets(adjustedSets + "-" + (adjustedSets + 1))
                .reps(adjustedReps + "-" + (adjustedReps + 3))
                .restTime(adjustedRestTime + "초")
                .build();
    }

    private int calculateAdjustedReps(int baseReps, double bfpAdjustment, double muscleAdjustment) {
        double adjustment = (bfpAdjustment + muscleAdjustment) / 2;
        // 기본값의 ±20% 범위 내에서 조정
        int adjustedValue = (int) Math.round(baseReps * adjustment);
        return Math.max(6, Math.min(20, adjustedValue)); // 최소 6회, 최대 20회
    }

    private int calculateAdjustedSets(int baseSets, double bmiAdjustment, String difficulty) {
        double difficultyMultiplier = switch (difficulty) {
            case "초급" -> 0.8;
            case "중급" -> 1.0;
            case "고급" -> 1.2;
            default -> 1.0;
        };

        // 기본값의 ±1 세트 범위 내에서 조정
        int adjustedValue = (int) Math.round(baseSets * bmiAdjustment * difficultyMultiplier);
        return Math.max(2, Math.min(5, adjustedValue)); // 최소 2세트, 최대 5세트
    }

    private int calculateAdjustedRestTime(int baseRestTime, double bmiAdjustment, double muscleAdjustment) {
        double adjustment = (bmiAdjustment + muscleAdjustment) / 2;
        // 기본값의 ±30초 범위 내에서 조정
        int adjustedValue = (int) Math.round(baseRestTime / adjustment);
        return Math.max(30, Math.min(180, adjustedValue)); // 최소 30초, 최대 180초
    }

    private double calculateBmiAdjustment(double bmi) {
        if (bmi < 18.5) return 0.85;      // 저체중: 약간 감소
        else if (bmi < 23) return 1.0;    // 정상: 기본
        else if (bmi < 25) return 0.9;    // 과체중: 감소
        else return 0.8;                  // 비만: 더 감소
    }

    private double calculateBfpAdjustment(double bodyFatPercentage) {
        if (bodyFatPercentage < 15) return 1.15;     // 낮은 체지방: 증가
        else if (bodyFatPercentage < 25) return 1.0;  // 정상 체지방: 기본
        else if (bodyFatPercentage < 30) return 0.9;  // 높은 체지방: 감소
        else return 0.8;                              // 매우 높은 체지방: 더 감소
    }

    private double calculateMuscleAdjustment(double skeletalMuscleMass) {
        if (skeletalMuscleMass < 20) return 0.85;      // 낮은 근육량: 감소
        else if (skeletalMuscleMass < 25) return 0.95;  // 약간 낮은 근육량: 약간 감소
        else if (skeletalMuscleMass < 30) return 1.0;   // 보통 근육량: 기본
        else if (skeletalMuscleMass < 35) return 1.1;   // 높은 근육량: 증가
        else return 1.2;                                // 매우 높은 근육량: 더 증가
    }


    private ExerciseRecommendationResponse createDefaultResponse(Member member, List<Exercise> exercises) {
        UserStats userStats = buildUserStats(member);
        List<ExerciseRecommendation> recommendations = exercises.stream()
                .limit(3)
                .map(exercise -> ExerciseRecommendation.builder()
                        .name(exercise.getName())
                        .difficulty(exercise.getDifficulty())
                        .description(exercise.getDescription())
                        .sets(exercise.getDefaultSets())
                        .reps(exercise.getDefaultReps())
                        .restTime(exercise.getDefaultRestTime())
                        .youtubeUrl(exercise.getYoutubeUrl())
                        .aiReasoning("기본 추천 운동입니다.")
                        .build())
                .collect(Collectors.toList());

        return ExerciseRecommendationResponse.builder()
                .userStats(userStats)
                .fitnessLevel(calculateFitnessLevel(userStats))
                .recommendations(recommendations)
                .aiAnalysis("기본적인 운동 루틴을 추천드립니다.")
                .build();
    }

    private String calculateFitnessLevel(UserStats stats) {
        if (stats.getBmi() < 18.5) return "저체중";
        else if (stats.getBmi() < 23) return "정상";
        else if (stats.getBmi() < 25) return "과체중";
        else return "비만";
    }

    private List<ExerciseRecommendation> createRecommendations(
            JsonNode recommendationsNode,
            List<Exercise> availableExercises,
            UserStats userStats) {

        List<ExerciseRecommendation> recommendations = new ArrayList<>();

        recommendationsNode.forEach(rec -> {
            String exerciseName = rec.get("name").asText();
            Exercise exercise = availableExercises.stream()
                    .filter(e -> e.getName().equals(exerciseName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("추천된 운동을 DB에서 찾을 수 없습니다: " + exerciseName));

            String sets;
            String reps;
            String restTime;

            try {
                // AI 추천값 사용 시도
                sets = rec.get("sets").asText();
                reps = rec.get("reps").asText();
                restTime = rec.get("restTime").asText();

                // AI 추천값이 비어있거나 유효하지 않은 경우 체크
                if (sets.isEmpty() || reps.isEmpty() || restTime.isEmpty()) {
                    throw new Exception("AI 추천값이 유효하지 않습니다");
                }
            } catch (Exception e) {
                // AI 추천값이 없거나 유효하지 않은 경우 calculateCustomIntensity 사용
                ExerciseIntensity customIntensity = calculateCustomIntensity(userStats, exercise);
                sets = customIntensity.getSets();
                reps = customIntensity.getReps();
                restTime = customIntensity.getRestTime();
            }

            recommendations.add(ExerciseRecommendation.builder()
                    .name(exercise.getName())
                    .difficulty(exercise.getDifficulty())
                    .description(exercise.getDescription())
                    .sets(sets)           // AI 추천값 또는 계산된 값
                    .reps(reps)          // AI 추천값 또는 계산된 값
                    .restTime(restTime)   // AI 추천값 또는 계산된 값
                    .youtubeUrl(exercise.getYoutubeUrl())
                    .aiReasoning(rec.get("aiReasoning").asText())
                    .build());
        });

        return recommendations;
    }

    // 기존의 유틸리티 메서드들은 그대로 유지
    private List<Exercise> findExercisesConsideringHealthConditions(String part, List<String> conditions, int limit) {
        if (conditions == null || conditions.isEmpty()) {
            return exerciseRepository.findByPart(part);
        }

        String conditionsPattern = conditions.stream()
                .map(condition -> condition + "[,]?")
                .collect(Collectors.joining("|"));

        return exerciseRepository.findSafeExercises(part, conditionsPattern, limit);
    }

    private UserStats buildUserStats(Member member) {
        return UserStats.builder()
                .height(Double.parseDouble(member.getMemberHeight()))
                .weight(Double.parseDouble(member.getMemberWeight()))
                .skeletalMuscleMass(Double.parseDouble(member.getSkeletalMuscleMass()))
                .bodyFatMass(Double.parseDouble(member.getBodyFatMass()))
                .bodyFatPercentage(Double.parseDouble(member.getBodyFatPercentage()))
                .bmi(Double.parseDouble(member.getBmi()))
                .build();
    }

    private void validateRequest(ExerciseRecommendationRequest request) {
        if (request.getTargetPart() == null || request.getExerciseCount() <= 0 || request.getExerciseCount() > 10) {
            throw new InvalidExerciseRequestException("잘못된 요청입니다.");
        }
    }
}