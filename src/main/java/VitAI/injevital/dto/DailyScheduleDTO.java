package VitAI.injevital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class DailyScheduleDTO {
    @NotNull(message = "회원 ID는 필수입니다")
    private String  memberId;

    @NotNull(message = "날짜는 필수입니다")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;
}