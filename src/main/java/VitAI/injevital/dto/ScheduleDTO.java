package VitAI.injevital.dto;

import VitAI.injevital.entity.Schedule;
import VitAI.injevital.entity.ScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
    private Long  id;
    private String memberId;
    private LocalDateTime scheduleDate;
    private String title;
    private String content;
    private ScheduleType type;
    private String color;

    // Entity -> DTO 변환
    public static ScheduleDTO from(Schedule entity) {
        return ScheduleDTO.builder()
                .id(entity.getId())
                .memberId(entity.getMember().getMemberId())
                .scheduleDate(entity.getScheduleDate())
                .title(entity.getTitle())
                .content(entity.getContent())
                .type(entity.getType())
                .color(entity.getColor())
                .build();
    }
}