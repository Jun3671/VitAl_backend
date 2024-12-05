package VitAI.injevital.controller;

import VitAI.injevital.dto.*;
import VitAI.injevital.entity.Schedule;
import VitAI.injevital.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ScheduleResponseDTO> createSchedule(@Valid @RequestBody ScheduleCreateDTO dto) {
        try {
            Schedule created = scheduleService.createSchedule(dto);
            return ResponseEntity.ok(ScheduleResponseDTO.of(
                    true,
                    "일정이 생성되었습니다",
                    List.of(ScheduleDTO.from(created))
            ));
        } catch (Exception e) {
            log.error("일정 생성 중 오류 발생", e);
            return ResponseEntity.badRequest().body(ScheduleResponseDTO.of(
                    false,
                    e.getMessage(),
                    null
            ));
        }
    }

    @PutMapping
    public ResponseEntity<ScheduleResponseDTO> updateSchedule(@Valid @RequestBody ScheduleUpdateDTO dto) {
        try {
            Schedule updated = scheduleService.updateSchedule(dto);
            return ResponseEntity.ok(ScheduleResponseDTO.of(
                    true,
                    "일정이 수정되었습니다",
                    List.of(ScheduleDTO.from(updated))
            ));
        } catch (Exception e) {
            log.error("일정 수정 중 오류 발생", e);
            return ResponseEntity.badRequest().body(ScheduleResponseDTO.of(
                    false,
                    e.getMessage(),
                    null
            ));
        }
    }

    @DeleteMapping
    public ResponseEntity<ScheduleResponseDTO> deleteSchedule(@Valid @RequestBody ScheduleDeleteDTO dto) {
        try {
            scheduleService.deleteSchedule(dto);
            return ResponseEntity.ok(ScheduleResponseDTO.of(
                    true,
                    "일정이 삭제되었습니다",
                    null
            ));
        } catch (Exception e) {
            log.error("일정 삭제 중 오류 발생", e);
            return ResponseEntity.badRequest().body(ScheduleResponseDTO.of(
                    false,
                    e.getMessage(),
                    null
            ));
        }
    }

    @GetMapping("/monthly")
    public ResponseEntity<ScheduleResponseDTO> getMonthlySchedules(@Valid @RequestBody MonthlyScheduleDTO dto) {
        try {
            List<Schedule> schedules = scheduleService.getMonthlySchedules(
                    dto.getMemberId(),
                    dto.getYear(),
                    dto.getMonth()
            );

            return ResponseEntity.ok(ScheduleResponseDTO.of(
                    true,
                    dto.getYear() + "년 " + dto.getMonth() + "월의 일정을 조회했습니다",
                    schedules.stream()
                            .map(ScheduleDTO::from)
                            .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            log.error("월간 일정 조회 중 오류 발생. memberId: {}, year: {}, month: {}",
                    dto.getMemberId(), dto.getYear(), dto.getMonth(), e);
            return ResponseEntity.badRequest().body(ScheduleResponseDTO.of(
                    false,
                    "월간 일정 조회 실패: " + e.getMessage(),
                    null
            ));
        }
    }

    @GetMapping("/daily")
    public ResponseEntity<ScheduleResponseDTO> getDailySchedules(@Valid @RequestBody DailyScheduleDTO dto) {
        try {
            List<Schedule> schedules = scheduleService.getDailySchedules(
                    dto.getMemberId(),
                    dto.getDate().toLocalDate()
            );

            return ResponseEntity.ok(ScheduleResponseDTO.of(
                    true,
                    dto.getDate().toLocalDate() + " 일자의 일정을 조회했습니다",
                    schedules.stream()
                            .map(ScheduleDTO::from)
                            .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            log.error("일간 일정 조회 중 오류 발생. memberId: {}, date: {}",
                    dto.getMemberId(), dto.getDate(), e);
            return ResponseEntity.badRequest().body(ScheduleResponseDTO.of(
                    false,
                    "일간 일정 조회 실패: " + e.getMessage(),
                    null
            ));
        }
    }
}