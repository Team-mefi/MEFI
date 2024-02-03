package com.mefi.backend.api.controller;

import com.mefi.backend.api.request.TeamReqDto;
import com.mefi.backend.api.response.MemberResDto;
import com.mefi.backend.api.response.TeamResDto;
import com.mefi.backend.api.service.TeamService;
import com.mefi.backend.common.auth.CustomUserDetails;
import com.mefi.backend.common.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/team")
@RequiredArgsConstructor
@Slf4j
@Tag(name="2.TEAM", description = "TEAM API")
public class TeamController {

    private final TeamService teamService;

    @PostMapping("")
    @Operation(summary = "팀생성", description = "팀 정보를 받아 팀 생성한다.")
    @ApiResponse(responseCode = "200", description = "성공 \n\n team 식별 아이디 반환")
    public ResponseEntity<? extends BaseResponseBody> createTeam(Authentication authentication, @RequestBody TeamReqDto teamReqDto) throws Exception{

        // 현재 사용자 식별 ID 불러옴
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        log.info("UserID :{}", user.getUserId());
        log.info("teamName :{}", teamReqDto.getTeamName());
        log.info("teamDescription :{}", teamReqDto.getTeamDescription());

        // 팀 생성 로직 호출
        teamService.createTeam(user.getUserId(), teamReqDto);

        // 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseBody.of(0,"Success"));
    }

    @GetMapping("")
    @Operation(summary = "팀 목록 조회", description = "사용자가 속한 팀 목록을 조회한다.")
    @ApiResponse(responseCode = "200", description = "성공 \n\n 사용자가 속한 팀 목록 반환")
    public ResponseEntity<? extends BaseResponseBody> getTeamList(Authentication authentication){

        // 현재 사용자 식별 ID 가져옴
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 파라미터 확인 로그
        log.info("getTeamList UserID :{}", user.getUserId());

        // 사용자 ID로 자신이 속한 팀 목록 조회 로직 호출
        List<TeamResDto> teamList = teamService.getTeamList(user.getUserId());

        // 사용자가 속한 팀 목록 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseBody.of(0,teamList));
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "팀원 목록 조회", description = "해당 팀의 팀원을 조회한다.")
    @ApiResponse(responseCode = "200", description = "성공 \n\n 팀의 팀원 리스트 반환")
    public ResponseEntity<? extends BaseResponseBody> getMemberList(Authentication authentication, @PathVariable(name = "teamId") Long teamId){

        // 현재 사용자 식별 ID 가져옴
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 파라미터 확인 로그
        log.info("userId : {}", user.getUserId());
        log.info("teamId : {}", teamId);

        // 팀 ID로 자신이 속한 팀원 목록 조회 로직
        List<MemberResDto> memberList = teamService.getMemberList(user.getUserId(), teamId);

        // 현재 팀의 팀원 목록 반환
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, memberList));
    }
}
