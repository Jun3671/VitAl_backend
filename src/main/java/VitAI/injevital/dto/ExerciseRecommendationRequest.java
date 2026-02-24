package VitAI.injevital.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class ExerciseRecommendationRequest {
    @NotNull(message = "회원 ID는 필수입니다")
    private String memberId;

    @NotNull(message = "운동 부위는 필수입니다")
    @Pattern(regexp = "어깨|등|가슴|하체|팔|복근", message = "올바른 운동 부위를 선택해주세요")
    private String targetPart;

    @Min(value = 1, message = "최소 1개 이상의 운동을 선택해야 합니다")
    @Max(value = 10, message = "최대 10개까지 운동을 선택할 수 있습니다")
    private int exerciseCount;

    private List<String> healthConditions;
}