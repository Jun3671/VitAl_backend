package VitAI.injevital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ScheduleDeleteDTO {
    @NotNull(message = "회원 ID는 필수입니다")
    private String memberId;

    @NotNull
    private Long scheduleId;
}