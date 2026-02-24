package VitAI.injevital.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExerciseRecommendation {
    private String name;
    private String difficulty;
    private String description;
    private String sets;
    private String reps;
    private String restTime;
    private String youtubeUrl;
    private String aiReasoning;  // AI가 이 운동을 추천한 이유
}