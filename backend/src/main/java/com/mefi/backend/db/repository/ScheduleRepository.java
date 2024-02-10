package com.mefi.backend.db.repository;

import com.mefi.backend.api.response.ScheduleTimeDto;
import com.mefi.backend.db.entity.PrivateSchedule;
import com.mefi.backend.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<PrivateSchedule, Long> {

    Optional<PrivateSchedule> findById(Long scheduleId);

    // 기간 내 개인 일정 조회
    List<PrivateSchedule> findByUserAndStartedTimeBetweenOrderByStartedTime(User user, LocalDateTime start, LocalDateTime end);

    // 해당 일자 팀원 전체 일정 조회
    @Query("SELECT new com.mefi.backend.api.response.ScheduleTimeDto(s.startedTime, s.endTime) " +
            "FROM PrivateSchedule s " +
            "WHERE s.user.id IN :userIds " +
            "AND FUNCTION('YEAR', s.startedTime) = FUNCTION('YEAR', :date) " +
            "AND FUNCTION('MONTH', s.startedTime) = FUNCTION('MONTH', :date) " +
            "AND FUNCTION('DAY', s.startedTime) = FUNCTION('DAY', :date) " +
            "ORDER BY s.startedTime")
    List<ScheduleTimeDto> findAllMemberSchedule(@Param("userIds") List<Long> members, @Param("date") LocalDateTime date);
}
