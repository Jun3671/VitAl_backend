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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 운동 추천 전체 플로우를 조정하는 메인 서비스
 * ExerciseCalculationService와 ExerciseAIService를 조합하여 사용합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final MemberRepository memberRepository;
    private final ExerciseCalculationService calculationService;
    private final ExerciseAIService aiService;

    @Value("${openai.model}")
    private String model;

    /**
     * 운동 추천 메인 메서드
     */
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
            ChatGPTResponse aiResponse = aiService.getAIRecommendations(chatGPTRequest, request.getTargetPart(), member);

            // 5. AI 응답 파싱 및 응답 생성
            return parseAIResponseAndCreateRecommendation(aiResponse, member, availableExercises);

        } catch (Exception e) {
            log.error("운동 추천 중 오류 발생", e);
            throw new RuntimeException("운동 추천을 처리할 수 없습니다.", e);
        }
    }

    /**
     * AI에 전송할 프롬프트를 생성합니다.
     */
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
                member.getMemberSmm(),
                member.getMemberBfm(),
                member.getMemberBfp(),
                member.getMemberBmi(),
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

    /**
     * AI 응답을 파싱하여 ExerciseRecommendationResponse를 생성합니다.
     */
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
                    userStats
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

    /**
     * AI 응답으로부터 ExerciseRecommendation 리스트를 생성합니다.
     */
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
                // AI 추천값이 없거나 유효하지 않은 경우 계산 서비스 사용
                ExerciseIntensity customIntensity = calculationService.calculateCustomIntensity(userStats, exercise);
                sets = customIntensity.getSets();
                reps = customIntensity.getReps();
                restTime = customIntensity.getRestTime();
            }

            recommendations.add(ExerciseRecommendation.builder()
                    .name(exercise.getName())
                    .difficulty(exercise.getDifficulty())
                    .description(exercise.getDescription())
                    .sets(sets)
                    .reps(reps)
                    .restTime(restTime)
                    .youtubeUrl(exercise.getYoutubeUrl())
                    .aiReasoning(rec.get("aiReasoning").asText())
                    .build());
        });

        return recommendations;
    }

    /**
     * 기본 응답을 생성합니다 (AI 파싱 실패 시 사용).
     */
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
                .fitnessLevel(calculationService.calculateFitnessLevel(userStats))
                .recommendations(recommendations)
                .aiAnalysis("기본적인 운동 루틴을 추천드립니다.")
                .build();
    }

    /**
     * 건강 상태를 고려하여 운동을 조회합니다.
     */
    private List<Exercise> findExercisesConsideringHealthConditions(String part, List<String> conditions, int limit) {
        if (conditions == null || conditions.isEmpty()) {
            return exerciseRepository.findByPart(part);
        }

        String conditionsPattern = conditions.stream()
                .map(condition -> condition + "[,]?")
                .collect(Collectors.joining("|"));

        return exerciseRepository.findSafeExercises(part, conditionsPattern, limit);
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
     * 요청값을 검증합니다.
     */
    private void validateRequest(ExerciseRecommendationRequest request) {
        if (request.getTargetPart() == null || request.getExerciseCount() <= 0 || request.getExerciseCount() > 10) {
            throw new InvalidExerciseRequestException("잘못된 요청입니다.");
        }
    }

    /**
     * YouTube URL을 정규화합니다.
     */
    @Transactional
    public void normalizeYoutubeUrls() {
        List<Exercise> exercises = exerciseRepository.findAll();
        for (Exercise exercise : exercises) {
            try {
                if (exercise.getYoutubeUrl() != null) {
                    exercise.setYoutubeUrl(exercise.getYoutubeUrl());
                }
            } catch (Exception e) {
                log.error("Exercise ID {} YouTube URL 정규화 실패: {}", exercise.getId(), e.getMessage());
            }
        }
        exerciseRepository.saveAll(exercises);
    }
}
