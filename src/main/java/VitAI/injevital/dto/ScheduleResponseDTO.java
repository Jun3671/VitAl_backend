package VitAI.injevital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDTO {
    private boolean success;
    private String message;
    private List<ScheduleDTO> schedules;

    public static ScheduleResponseDTO of(boolean success, String message, List<ScheduleDTO> schedules) {
        return ScheduleResponseDTO.builder()
                .success(success)
                .message(message)
                .schedules(schedules)
                .build();
    }
}