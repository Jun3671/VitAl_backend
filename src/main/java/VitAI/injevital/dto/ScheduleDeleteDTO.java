package VitAI.injevital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
public class ScheduleDeleteDTO {
    private String memberId;
    private LocalDate scheduleDate;
    private String content;;
}