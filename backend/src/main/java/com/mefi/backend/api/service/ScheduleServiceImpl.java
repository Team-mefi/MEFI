package com.mefi.backend.api.service;

import com.mefi.backend.api.request.ScheduleReqDto;
import com.mefi.backend.api.response.ScheduleDetailResDto;
import com.mefi.backend.api.response.ScheduleResDto;
import com.mefi.backend.api.response.ScheduleTimeDto;
import com.mefi.backend.common.exception.ErrorCode;
import com.mefi.backend.common.exception.Exceptions;
import com.mefi.backend.db.entity.*;
import com.mefi.backend.db.repository.ScheduleRepository;
import com.mefi.backend.db.repository.TeamUserRepository;
import com.mefi.backend.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements  ScheduleService{

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final TeamUserRepository teamUserRepository;

    @Override
    @Transactional
    public void createSchedule(Long userId, ScheduleReqDto scheduleReqDto) {
        // 유저 조회, 해당 회원이 존재하지 않으면 예외 처리
        User user = userRepository.findById(userId).orElseThrow(()->new Exceptions(ErrorCode.MEMBER_NOT_EXIST));

        // 일정 등록
        PrivateSchedule privateSchedule = PrivateSchedule.builder()
                .user(user)
                .startedTime(scheduleReqDto.getStartedTime())
                .endTime(scheduleReqDto.getEndTime())
                .summary(scheduleReqDto.getSummary())
                .description(scheduleReqDto.getDescription())
                .type(scheduleReqDto.getType())
                .build();

        // 일정 등록
        scheduleRepository.save(privateSchedule);
    }

    @Override
    @Transactional
    public ScheduleResDto deleteSchedule(Long userId, Long alarmId) {
        // 삭제하려는 일정 조회, 존재하지 않는다면 예외 발생
        PrivateSchedule schedule = scheduleRepository.findById(alarmId).orElseThrow(()->new Exceptions(ErrorCode.SCHEDULE_NOT_EXIST));

        // 일정을 등록한 유저가 아니라면 예외 발생
        if(userId != schedule.getUser().getId()){
            throw new Exceptions(ErrorCode.SCHEDULE_ACCESS_DENIED);
        }

        // 일정 삭제
        scheduleRepository.delete(schedule);
        return new ScheduleResDto(schedule);
    }

    // 기간 내 개인 일정 조회
    @Override
    public List<ScheduleDetailResDto> getPrivateSchedule(Long userId, String start, String end) {

        log.info("=======================ScheduleService-getPrivateSchedule()=======================");

        // 사용자 조회
        User user = userRepository.findById(userId).orElseThrow(() -> new Exceptions(ErrorCode.USER_NOT_EXIST));

        // 시작 및 종료 일시 파싱
        LocalDateTime s = LocalDateTime.parse(start + "000000.000", DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSS"));
        LocalDateTime e = LocalDateTime.parse(end + "235959.999", DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSS"));

        // 시작 및 종료 일시 로깅
        log.info("start : {}", s);
        log.info("end : {}", e);

        // DB 조회
        List<PrivateSchedule> result = scheduleRepository.findByUserAndStartedTimeBetweenOrderByStartedTime(user, s, e);

        // 엔티티를 DTO로 변환
        List<ScheduleDetailResDto> list = new ArrayList<>();
        for (PrivateSchedule ps : result) {
            list.add(new ScheduleDetailResDto(ps.getId(), ps.getSummary(), ps.getDescription(), ps.getStartedTime() ,ps.getEndTime(), ps.getType()));
        }

        // DTO 리스트 반환
        return list;
    }

    // 해당 일자 팀원 전체 일정 조회
    @Override
    public List<ScheduleTimeDto> getAllMemberSchedule(Long userId, Long teamId, String day) {

        log.info("=======================ScheduleService-getAllMemberSchedule()=======================");

        // 팀원이 아닌 경우 예외 처리
        UserTeam userTeam  = teamUserRepository.findMember(userId, teamId).orElseThrow(() -> new Exceptions(ErrorCode.TEAM_ACCESS_DENIED));

        // 팀 리더가 아닌 경우 예외 처리
        if(userTeam.getRole() != UserRole.LEADER) throw new Exceptions(ErrorCode.NOT_TEAM_LEADER);

        // 팀원들의 PK 값 조회 및 로깅
        List<Long> memberIds = teamUserRepository.findByTeamId(teamId);
        log.info("memberIds : {}", memberIds);

        // 조회 일자 파싱 및 로깅
        LocalDateTime date = LocalDateTime.parse(day + "000000.000", DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSS"));
        log.info("date {}", date);

        // 팀원들의 일정 조회
        return scheduleRepository.findAllMemberSchedule(memberIds, date);
    }

    // 개인 일정 수정
    @Override
    @Transactional
    public void modifySchedule(Long userId, ScheduleReqDto scheduleReqDto, Long scheduleId) {

        log.info("=======================ScheduleService-modifySchedule()=======================");

        // 삭제하려는 일정 조회, 존재하지 않는다면 예외 발생
        PrivateSchedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(()->new Exceptions(ErrorCode.SCHEDULE_NOT_EXIST));

        // 일정을 등록한 유저가 아니라면 예외 발생
        if(userId != schedule.getUser().getId()) throw new Exceptions(ErrorCode.SCHEDULE_ACCESS_DENIED);

        // 만약 수정하려는 일정 타입이 회의라면 예외 발생
        if(schedule.getType() == ScheduleType.CONFERENCE) throw new Exceptions(ErrorCode.CONFERENCE_NOT_MODIFY);

        // 일정 수정
        schedule.changeDetail(scheduleReqDto.getSummary(), scheduleReqDto.getDescription(), scheduleReqDto.getStartedTime(), scheduleReqDto.getEndTime());
    }
}
