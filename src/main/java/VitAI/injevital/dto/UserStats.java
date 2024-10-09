package VitAI.injevital.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStats {
    private double height;
    private double weight;
    private double skeletalMuscleMass;
    private double bodyFatMass;
    private double bodyFatPercentage;
    private double bmi;
}