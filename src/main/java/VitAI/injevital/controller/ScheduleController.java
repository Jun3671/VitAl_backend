package VitAI.injevital.controller;

import VitAI.injevital.dto.*;
import VitAI.injevital.entity.Schedule;
import VitAI.injevital.service.ScheduleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    final private ScheduleService scheduleService;

    @PostMapping("/create")
    public ResponseEntity<ScheduleResponseDTO> createSchedule(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String memberId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate scheduleDate) {
        try {
            Schedule created = scheduleService.createSchedule(
                    ScheduleCreateDTO.builder()
                            .title(title)
                            .content(content)
                            .memberId(memberId)
                            .scheduleDate(scheduleDate.atStartOfDay())
                            .build()
            );
            return ResponseEntity.ok(ScheduleResponseDTO.of(
                    true,
                    "일정이 생성되었습니다",
                    List.of(ScheduleDTO.from(created))
            ));
        } catch (Exception e) {
            log.error("일정 생성 중 오류 발생. memberId: {}", memberId, e);
            return ResponseEntity.badRequest().body(ScheduleResponseDTO.of(
                    false,
                    e.getMessage(),
                    null
            ));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ScheduleResponseDTO> updateSchedule(
            @RequestParam Long scheduleId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String memberId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate scheduleDate) {
        try {
            Schedule updated = scheduleService.updateSchedule(
                    ScheduleUpdateDTO.builder()
                            .scheduleId(scheduleId)
                            .title(title)
                            .content(content)
                            .memberId(memberId)
                            .scheduleDate(scheduleDate.atStartOfDay())
                            .build()
            );
            return ResponseEntity.ok(ScheduleResponseDTO.of(
                    true,
                    "일정이 수정되었습니다",
                    List.of(ScheduleDTO.from(updated))
            ));
        } catch (Exception e) {
            log.error("일정 수정 중 오류 발생. scheduleId: {}, memberId: {}", scheduleId, memberId, e);
            return ResponseEntity.badRequest().body(ScheduleResponseDTO.of(
                    false,
                    e.getMessage(),
                    null
            ));
        }
    }

    // 삭제 메서드는 이미 RequestParam으로 되어있어서 그대로 유지
    @DeleteMapping("/delete")
    public ResponseEntity<ScheduleResponseDTO> deleteSchedule(
            @RequestParam @NotNull(message = "일정 ID는 필수입니다") Long scheduleId,
            @RequestParam @NotNull(message = "회원 ID는 필수입니다") String memberId) {
        try {
            scheduleService.deleteSchedule(
                    ScheduleDeleteDTO.builder()
                            .scheduleId(scheduleId)
                            .memberId(memberId)
                            .build()
            );
            return ResponseEntity.ok(ScheduleResponseDTO.of(
                    true,
                    "일정이 삭제되었습니다",
                    null
            ));
        } catch (Exception e) {
            log.error("일정 삭제 중 오류 발생. scheduleId: {}, memberId: {}", scheduleId, memberId, e);
            return ResponseEntity.badRequest().body(ScheduleResponseDTO.of(
                    false,
                    e.getMessage(),
                    null
            ));
        }
    }
}