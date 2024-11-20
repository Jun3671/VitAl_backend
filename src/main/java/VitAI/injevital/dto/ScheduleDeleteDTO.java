package VitAI.injevital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleDeleteDTO {
    @NotNull(message = "회원 ID는 필수입니다")
    private String memberId;

    @NotNull(message = "일정 날짜는 필수입니다")
    private LocalDateTime scheduleDate;
}