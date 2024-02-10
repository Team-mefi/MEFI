package com.mefi.backend.api.service;

import com.mefi.backend.api.request.TeamModifyReqDto;
import com.mefi.backend.api.request.TeamReqDto;
import com.mefi.backend.api.response.MemberResDto;
import com.mefi.backend.api.response.TeamDetailDto;
import com.mefi.backend.api.response.TeamResDto;
import com.mefi.backend.common.exception.ErrorCode;
import com.mefi.backend.common.exception.Exceptions;
import com.mefi.backend.db.entity.Team;
import com.mefi.backend.db.entity.User;
import com.mefi.backend.db.entity.UserRole;
import com.mefi.backend.db.entity.UserTeam;
import com.mefi.backend.db.repository.TeamRepository;
import com.mefi.backend.db.repository.TeamUserRepository;
import com.mefi.backend.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService{

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamUserRepository teamUserRepository;

    // 팀 생성
    @Override
    @Transactional
    public void createTeam(Long leaderId, TeamReqDto teamReqDto){
        log.info("=======================TeamService-createTeam()=======================");

        // 팀 생성
        Team team = Team.builder()
                .name(teamReqDto.getTeamName())
                .description(teamReqDto.getTeamDescription())
                .createdTime(LocalDateTime.now())
                .build();

        // 리더 추가
        User leader = userRepository.findById(leaderId).orElseThrow(() -> new Exceptions(ErrorCode.USER_NOT_EXIST));
        UserTeam teamLeader = UserTeam.builder()
                .user(leader)
                .team(team)
                .role(UserRole.LEADER)
                .build();
        team.addUserTeam(teamLeader);

        // 팀원 추가
        List<Long> memberIds = teamReqDto.getMembers();
        for (Long memberId : memberIds) {
            User member = userRepository.findById(memberId).orElseThrow(() -> new Exceptions(ErrorCode.USER_NOT_EXIST));
            UserTeam teamMember = UserTeam.builder()
                    .user(member)
                    .team(team)
                    .role(UserRole.MEMBER)
                    .build();
            team.addUserTeam(teamMember);
        }

        // 팀 저장
        teamRepository.save(team);
    }

    // 팀 목록 조회
    @Override
    public List<TeamResDto> getTeamList(Long userId) {
        log.info("=======================TeamService-getTeamList()=======================");

        // 현재 사용자가 속한 팀 정보 리스트 반환
        return teamUserRepository.findTeamsByUserId(userId);
    }

    // 팀원 목록 조회
    @Override
    public List<MemberResDto> getMemberList(Long userId, Long teamId) {
        log.info("=======================TeamService-getMemberList()=======================");

        // 해당 팀의 팀원가 아니라면 예외 처리
        teamUserRepository.findMember(userId, teamId).orElseThrow(() -> new Exceptions(ErrorCode.TEAM_ACCESS_DENIED));

        // 팀 구성원 목록 반환
        return teamUserRepository.getMemberList(teamId);
    }

    // 팀 역할 조회
    @Override
    public Boolean checkRole(Long userId, Long teamId) {
        log.info("=======================TeamService-checkRole()=======================");

        // 해당 팀의 팀원가 아니라면 예외 처리
        UserTeam userTeam  = teamUserRepository.findMember(userId, teamId).orElseThrow(() -> new Exceptions(ErrorCode.TEAM_ACCESS_DENIED));

        // 해당 유저의 권한 로깅
        log.info("해당 유저의 권한 : {}", userTeam.getRole());

        // 만약 해당 유저가 팀의 리더가 아니라면 예외 처리
        if(userTeam.getRole() != UserRole.LEADER) throw new Exceptions(ErrorCode.NOT_TEAM_LEADER);

        // 리더라면 true 반환
        return true;
    }

    // 팀원 추가
    @Override
    @Transactional
    public void addMember(Long userId, Long teamId, Long memberId) {
        log.info("=======================TeamService-addMember()=======================");

        // 팀장 권환 확인
        checkRole(userId, teamId);

        // 추가할 팀원 또는 팀이 존재하지 않으면 예외 처리
        User member = userRepository.findById(memberId).orElseThrow(() -> new Exceptions(ErrorCode.USER_NOT_EXIST));
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new Exceptions(ErrorCode.TEAM_NOT_EXIST));

        // 팀원 추가
        UserTeam teamMember = UserTeam.builder()
                .user(member)
                .team(team)
                .role(UserRole.MEMBER)
                .build();

        // 팀원 저장
        teamUserRepository.save(teamMember);
    }

    // 팀 삭제
    @Override
    @Transactional
    public void deleteTeam(Long userId, Long teamId) {
        log.info("=======================TeamService-deleteTeam()=======================");

        // 해당 팀이 존재하지 않으면 예외 처리
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new Exceptions(ErrorCode.TEAM_NOT_EXIST));

        // 팀장 권환 확인
        checkRole(userId, teamId);

        // 팀 삭제 (CascadeType.ALL 옵션으로 멤버들도 함께 삭제)
        teamRepository.delete(team);
    }

    // 팀원 삭제
    @Override
    @Transactional
    public void deleteMember(Long userId, Long teamId, Long memberId) {
        log.info("=======================TeamService-deleteMember()=======================");

        // 팀장 권환 확인
        checkRole(userId, teamId);

        // 만약 리더를 삭제하려 한다면 예외 발생
        UserTeam userTeam  = teamUserRepository.findMember(memberId, teamId).orElseThrow(() -> new Exceptions(ErrorCode.MEMBER_NOT_EXIST));
        if(userTeam.getRole() == UserRole.LEADER){throw new Exceptions(ErrorCode.LEADER_NOT_DELETEABLE);}

        // 삭제하려는 팀원이 해당 팀에 없다면 예외 발생
        teamUserRepository.findMember(memberId, teamId).orElseThrow(() -> new Exceptions(ErrorCode.TEAM_ACCESS_DENIED));

        // 팀원 삭제
        teamUserRepository.deleteMember(memberId, teamId);
    }

    // 팀 상세 정보 조회
    @Override
    public TeamDetailDto getTeamDetail(Long userId, Long teamId) {
        log.info("=======================TeamService-getTeamDetail()=======================");

        // 해당 팀의 팀원이 아니라면 예외 처리
        teamUserRepository.findMember(userId, teamId).orElseThrow(() -> new Exceptions(ErrorCode.TEAM_ACCESS_DENIED));

        // 해당 팀이 존재하지 않는다면 예외 처리
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new Exceptions(ErrorCode.TEAM_NOT_EXIST));

        // 팀 상세 정보 반환
        return new TeamDetailDto(team.getId(), team.getName(), team.getDescription(), team.getCreatedTime());
    }

    // 팀 정보 수정
    @Override
    @Transactional
    public void modifyTeam(Long userId, Long teamId, TeamModifyReqDto teamModifyReqDto) {
        log.info("=======================TeamService-modifyTeam()=======================");

        // 해당 팀이 존재하지 않는다면 예외 처리
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new Exceptions(ErrorCode.TEAM_NOT_EXIST));

        // 팀장 권환 확인
        checkRole(userId, teamId);

        // 팀 정보 수정
        team.modifyInfo(teamModifyReqDto.getName(), teamModifyReqDto.getDescription());
    }

    // 리더 권한 변경
    @Override
    @Transactional
    public void modifyUserRole(Long userId, Long teamId, Long memberId) {
        log.info("=======================TeamService-modifyUserRole()=======================");

        // 팀장 권환 확인
        checkRole(userId, teamId);

        // 양도자와 피양도자 해당 팀의 팀원이 아니라면 예외 처리
        UserTeam leader = teamUserRepository.findMember(userId, teamId).orElseThrow(() -> new Exceptions(ErrorCode.TEAM_ACCESS_DENIED));
        UserTeam member = teamUserRepository.findMember(memberId,teamId).orElseThrow(() -> new Exceptions(ErrorCode.TEAM_ACCESS_DENIED));;

        // 역할 수정
        leader.changeRole(UserRole.MEMBER);
        member.changeRole(UserRole.LEADER);
    }
}
