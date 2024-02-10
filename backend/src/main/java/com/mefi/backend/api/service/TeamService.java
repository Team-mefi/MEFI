package com.mefi.backend.api.service;

import com.mefi.backend.api.request.TeamModifyReqDto;
import com.mefi.backend.api.request.TeamReqDto;
import com.mefi.backend.api.response.MemberResDto;
import com.mefi.backend.api.response.TeamDetailDto;
import com.mefi.backend.api.response.TeamResDto;

import java.util.List;

public interface TeamService {

    // 팀 생성
    void createTeam(Long learderId, TeamReqDto teamReqDto);

    // 팀 목록 조회
    List<TeamResDto> getTeamList(Long userId);

    // 팀원 목록 조회
    List<MemberResDto> getMemberList(Long userId, Long teamId);

    // 팀 역할 조회
    Boolean checkRole(Long userId, Long teamId);

    // 팀원 추가
    void addMember(Long userId, Long teamId, Long memberId);

    // 팀 삭제
    void deleteTeam(Long userId, Long teamId);

    // 팀원 삭제
    void deleteMember(Long userId, Long teamId, Long memberId);

    // 팀 상세 정보 조회
    TeamDetailDto getTeamDetail(Long userId, Long teamId);

    // 팀 정보 수정
    void modifyTeam(Long userId, Long teamId, TeamModifyReqDto teamModifyReqDto);

    // 리더 권한 변경
    void modifyUserRole(Long userId, Long teamId, Long memberId);
}