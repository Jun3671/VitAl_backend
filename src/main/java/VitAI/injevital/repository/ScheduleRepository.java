package VitAI.injevital.repository;

import VitAI.injevital.entity.Member;
import VitAI.injevital.entity.Schedule;
import VitAI.injevital.entity.ScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByMemberAndScheduleDateBetween(
            Member member,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Schedule> findByMemberAndScheduleDateBetweenOrderByScheduleDate(
            Member member,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    // 특정 회원의 특정 날짜 일정 찾기
    Optional<Schedule> findByMemberMemberIdAndScheduleDate(String memberId, LocalDateTime scheduleDate);

    List<Schedule> findByMemberOrderByScheduleDateDesc(Member member);

    Optional<Schedule> findByMemberMemberIdAndScheduleDateAndContent(
            String memberId,
            LocalDate scheduleDate,
            String content
    );

}