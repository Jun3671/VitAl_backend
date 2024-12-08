package VitAI.injevital.service;

import VitAI.injevital.dto.ScheduleCreateDTO;
import VitAI.injevital.dto.ScheduleDeleteDTO;
import VitAI.injevital.dto.ScheduleUpdateDTO;
import VitAI.injevital.entity.Member;
import VitAI.injevital.entity.Schedule;
import VitAI.injevital.repository.MemberRepository;
import VitAI.injevital.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@Builder
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;

    public Schedule createSchedule(ScheduleCreateDTO dto) {
        Member member = memberRepository.findByMemberId(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Schedule schedule = new Schedule();
        schedule.setMember(member);
        schedule.setScheduleDate(dto.getScheduleDate());
        schedule.setTitle(dto.getTitle());
        schedule.setContent(dto.getContent());
        schedule.setType(dto.getType());

        return scheduleRepository.save(schedule);
    }

    public Schedule updateSchedule(ScheduleUpdateDTO dto) {
        // 해당 회원의 해당 날짜 일정 찾기
        Schedule schedule = scheduleRepository.findByMemberMemberIdAndScheduleDate(
                        dto.getMemberId(), dto.getScheduleDate())
                .orElseThrow(() -> new RuntimeException("해당 일정을 찾을 수 없습니다."));

        try {
            // 일정 정보 업데이트
            schedule.setTitle(dto.getTitle());
            schedule.setContent(dto.getContent());
            schedule.setType(dto.getType());

            return scheduleRepository.save(schedule);
        } catch (Exception e) {
            log.error("일정 업데이트 중 오류 발생. memberId: {}, date: {}",
                    dto.getMemberId(), dto.getScheduleDate(), e);
            throw new RuntimeException("일정 업데이트에 실패했습니다: " + e.getMessage());
        }
    }

    public void deleteSchedule(ScheduleDeleteDTO dto) {
        try {
            Schedule schedule = scheduleRepository.findById(dto.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("해당 일정을 찾을 수 없습니다."));

            // 권한 체크: 해당 memberId의 일정인지 확인
            if (!schedule.getMember().getMemberId().equals(dto.getMemberId())) {
                throw new RuntimeException("일정 삭제 권한이 없습니다.");
            }

            scheduleRepository.delete(schedule);
        } catch (Exception e) {
            log.error("일정 삭제 중 오류 발생. scheduleId: {}, memberId: {}",
                    dto.getScheduleId(), dto.getMemberId(), e);
            throw new RuntimeException("일정 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    public List<Schedule> getMonthlySchedules(String memberId, int year, int month) {
        try {
            // 회원 존재 여부 확인
            Member member = memberRepository.findByMemberId(memberId)
                    .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다. ID: " + memberId));

            LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

            List<Schedule> schedules = scheduleRepository.findByMemberAndScheduleDateBetween(
                    member, startOfMonth, endOfMonth);

            log.info("월간 일정 조회 완료. memberId: {}, year: {}, month: {}, count: {}",
                    memberId, year, month, schedules.size());

            return schedules;
        } catch (Exception e) {
            log.error("월간 일정 조회 중 오류 발생. memberId: {}, year: {}, month: {}",
                    memberId, year, month, e);
            throw new RuntimeException("월간 일정 조회에 실패했습니다: " + e.getMessage());
        }
    }

    public List<Schedule> getDailySchedules(String memberId, LocalDate date) {
        try {
            // 회원 존재 여부 확인
            Member member = memberRepository.findByMemberId(memberId)
                    .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다. ID: " + memberId));

            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            List<Schedule> schedules = scheduleRepository.findByMemberAndScheduleDateBetweenOrderByScheduleDate(
                    member, startOfDay, endOfDay);

            log.info("일간 일정 조회 완료. memberId: {}, date: {}, count: {}",
                    memberId, date, schedules.size());

            return schedules;
        } catch (Exception e) {
            log.error("일간 일정 조회 중 오류 발생. memberId: {}, date: {}",
                    memberId, date, e);
            throw new RuntimeException("일간 일정 조회에 실패했습니다: " + e.getMessage());
        }
    }
}