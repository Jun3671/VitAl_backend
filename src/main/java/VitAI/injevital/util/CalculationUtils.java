package VitAI.injevital.util;

/**
 * 건강 지표 계산을 위한 유틸리티 클래스
 */
public class CalculationUtils {

    private CalculationUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * BMI(체질량지수)를 계산합니다.
     * BMI = 체중(kg) / (신장(m))^2
     *
     * @param heightCm 키 (cm)
     * @param weightKg 체중 (kg)
     * @return 계산된 BMI (소수점 첫째자리까지)
     */
    public static double calculateBmi(double heightCm, double weightKg) {
        if (heightCm <= 0 || weightKg <= 0) {
            throw new IllegalArgumentException("키와 체중은 0보다 커야 합니다.");
        }

        double heightInMeters = heightCm / 100.0;
        return Math.round((weightKg / (heightInMeters * heightInMeters)) * 10) / 10.0;
    }
}
