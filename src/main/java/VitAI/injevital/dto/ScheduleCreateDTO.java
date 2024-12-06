package VitAI.injevital.dto;

import VitAI.injevital.entity.ScheduleType;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// 일정 생성 DTO
@Data
public class ScheduleCreateDTO {
    @NotNull(message = "회원 ID는 필수입니다")
    private String   memberId;

    @NotNull(message = "일정 날짜는 필수입니다")
    private LocalDateTime scheduleDate;

    @NotNull(message = "일정 제목은 필수입니다")
    private String title;

    private String content;

    @NotNull(message = "일정 타입은 필수입니다")
    private ScheduleType type;  // color 필드 제거
}