package VitAI.injevital.service;

import VitAI.injevital.dto.ExerciseIntensity;
import VitAI.injevital.dto.UserStats;
import VitAI.injevital.entity.Exercise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 운동 강도 계산을 담당하는 서비스
 * BMI, 체지방률, 골격근량을 기반으로 개인화된 운동 강도를 계산합니다.
 */
@Service
@Slf4j
public class ExerciseCalculationService {

    /**
     * 사용자 신체 정보를 기반으로 맞춤형 운동 강도를 계산합니다.
     */
    public ExerciseIntensity calculateCustomIntensity(UserStats userStats, Exercise exercise) {
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

    /**
     * 체력 수준을 계산합니다.
     */
    public String calculateFitnessLevel(UserStats stats) {
        if (stats.getBmi() < 18.5) return "저체중";
        else if (stats.getBmi() < 23) return "정상";
        else if (stats.getBmi() < 25) return "과체중";
        else return "비만";
    }

    /**
     * BMI 기반 운동 강도 조정 계수를 계산합니다.
     */
    private double calculateBmiAdjustment(double bmi) {
        if (bmi < 18.5) return 0.85;      // 저체중: 약간 감소
        else if (bmi < 23) return 1.0;    // 정상: 기본
        else if (bmi < 25) return 0.9;    // 과체중: 감소
        else return 0.8;                  // 비만: 더 감소
    }

    /**
     * 체지방률 기반 운동 강도 조정 계수를 계산합니다.
     */
    private double calculateBfpAdjustment(double bodyFatPercentage) {
        if (bodyFatPercentage < 15) return 1.15;     // 낮은 체지방: 증가
        else if (bodyFatPercentage < 25) return 1.0;  // 정상 체지방: 기본
        else if (bodyFatPercentage < 30) return 0.9;  // 높은 체지방: 감소
        else return 0.8;                              // 매우 높은 체지방: 더 감소
    }

    /**
     * 골격근량 기반 운동 강도 조정 계수를 계산합니다.
     */
    private double calculateMuscleAdjustment(double skeletalMuscleMass) {
        if (skeletalMuscleMass < 20) return 0.85;      // 낮은 근육량: 감소
        else if (skeletalMuscleMass < 25) return 0.95;  // 약간 낮은 근육량: 약간 감소
        else if (skeletalMuscleMass < 30) return 1.0;   // 보통 근육량: 기본
        else if (skeletalMuscleMass < 35) return 1.1;   // 높은 근육량: 증가
        else return 1.2;                                // 매우 높은 근육량: 더 증가
    }

    /**
     * 조정된 세트 수를 계산합니다.
     */
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

    /**
     * 조정된 반복 횟수를 계산합니다.
     */
    private int calculateAdjustedReps(int baseReps, double bfpAdjustment, double muscleAdjustment) {
        double adjustment = (bfpAdjustment + muscleAdjustment) / 2;
        // 기본값의 ±20% 범위 내에서 조정
        int adjustedValue = (int) Math.round(baseReps * adjustment);
        return Math.max(6, Math.min(20, adjustedValue)); // 최소 6회, 최대 20회
    }

    /**
     * 조정된 휴식 시간을 계산합니다.
     */
    private int calculateAdjustedRestTime(int baseRestTime, double bmiAdjustment, double muscleAdjustment) {
        double adjustment = (bmiAdjustment + muscleAdjustment) / 2;
        // 기본값의 ±30초 범위 내에서 조정
        int adjustedValue = (int) Math.round(baseRestTime / adjustment);
        return Math.max(30, Math.min(180, adjustedValue)); // 최소 30초, 최대 180초
    }
}
