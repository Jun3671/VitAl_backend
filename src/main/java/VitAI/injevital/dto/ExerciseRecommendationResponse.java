package VitAI.injevital.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExerciseRecommendationResponse {
    private UserStats userStats;
    private String fitnessLevel;
    private List<ExerciseRecommendation> recommendations;
    private String aiAnalysis;
}