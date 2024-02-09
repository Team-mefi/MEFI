package com.mefi.backend.api.controller;

import com.mefi.backend.api.request.TeamModifyReqDto;
import com.mefi.backend.api.request.TeamReqDto;
import com.mefi.backend.api.response.MemberResDto;
import com.mefi.backend.api.response.TeamDetailDto;
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
@RequestMapping("/api/team")
@RequiredArgsConstructor
@Slf4j
@Tag(name="2.TEAM", description = "TEAM API")
public class TeamController {

    private final TeamService teamService;

    @PostMapping("")
    @Operation(summary = "팀 생성", description = "팀 정보를 받아 팀 생성한다.")
    @ApiResponse(responseCode = "201", description = "성공 \n\n 'Success' 반환")
    public ResponseEntity<? extends BaseResponseBody> createTeam(Authentication authentication,
                                                                 @RequestBody TeamReqDto teamReqDto){

        log.info("=======================Team Controller-createTeam()=======================");

        // 현재 사용자의 세부 정보를 인증 객체에서 추출하여 저장
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 사용자 식별자와 팀 생성 요청 데이터 로깅
        log.info("userId : {}", user.getUserId());
        log.info("teamName : {}", teamReqDto.getTeamName());
        log.info("teamDescription : {}", teamReqDto.getTeamDescription());

        // 팀 생성 서비스 호출 및 사용자 식별자와 요청 데이터 전달
        teamService.createTeam(user.getUserId(), teamReqDto);

        // 생성이 성공하면 HTTP 상태 코드 201(Created) 및 성공 메시지를 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseBody.of(0,"Success"));
    }

    @GetMapping("")
    @Operation(summary = "팀 목록 조회", description = "사용자가 속한 팀 목록을 조회한다.")
    @ApiResponse(responseCode = "200", description = "성공 \n\n 사용자가 속한 팀 목록 반환")
    public ResponseEntity<? extends BaseResponseBody> getTeamList(Authentication authentication){

        log.info("=======================Team Controller-getTeamList()=======================");

        // 현재 사용자의 세부 정보를 인증 객체에서 추출하여 저장
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 사용자 식별자 로깅
        log.info("userId : {}", user.getUserId());

        // 팀 목록 조회 서비스 호출 및 사용자 식별자 전달
        List<TeamResDto> teamList = teamService.getTeamList(user.getUserId());

        // 조회가 성공하면 HTTP 상태 코드 200(OK)을 반환하고, 사용자가 속한 팀 목록 반환
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0,teamList));
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "팀원 목록 조회", description = "해당 팀의 팀원을 조회한다.")
    @ApiResponse(responseCode = "200", description = "성공 \n\n 팀의 팀원 리스트 반환")
    public ResponseEntity<? extends BaseResponseBody> getMemberList(Authentication authentication,
                                                                    @PathVariable(name = "teamId") Long teamId){

        log.info("=======================Team Controller-getMemberList()=======================");

        // 현재 사용자의 세부 정보를 인증 객체에서 추출하여 저장
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 사용자, 팀 식별자 로깅
        log.info("userId : {}", user.getUserId());
        log.info("teamId : {}", teamId);

        // 팀원 목록 조회 서비스 호출 및 사용자, 팀 식별자 전달
        List<MemberResDto> memberList = teamService.getMemberList(user.getUserId(), teamId);

        // 조회가 성공하면 HTTP 상태 코드 200(OK) 및 팀의 팀원 목록을 반환
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, memberList));
    }

    @PostMapping("/{teamId}/{memberId}")
    @Operation(summary = "팀원 추가", description = "팀장이 속한 팀의 해당 유저를 추가한다.")
    @ApiResponse(responseCode = "201", description = "성공 \n\n 'Success' 반환")
    public ResponseEntity<? extends BaseResponseBody> addMember(Authentication authentication,
                                                                @PathVariable(name = "teamId") Long teamId,
                                                                @PathVariable(name = "memberId") Long memberId){

        log.info("=======================Team Controller-addMember()=======================");

        // 현재 사용자의 세부 정보를 인증 객체에서 추출하여 저장
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 사용자, 팀, 추가 멤버 식별자 로깅
        log.info("userId : {}", user.getUserId());
        log.info("teamId : {}", teamId);
        log.info("memberId : {}", memberId);

        // 팀원 추가 서비스 호출 및 사용자, 팀, 추가 멤버 식별자 전달
        teamService.addMember(user.getUserId(), teamId, memberId);

        // 멤버 추가를 성공하면 HTTP 상태 코드 201(CREATED) 및 성공 메시지를 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseBody.of(0, "Success"));
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "팀 삭제", description = "팀을 삭제한다")
    @ApiResponse(responseCode = "204", description = "성공 \n\n 'Success' 반환")
    public ResponseEntity<? extends BaseResponseBody> deleteTeam(Authentication authentication,
                                                                 @PathVariable(name = "teamId") Long teamId){

        log.info("=======================Team Controller-deleteTeam()=======================");

        // 현재 사용자의 세부 정보를 인증 객체에서 추출하여 저장
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 사용자, 팀 식별자 로깅
        log.info("userId : {}", user.getUserId());
        log.info("teamId : {}", teamId);

        // 팀 삭제 서비스 호출 및 사용자, 팀 식별자 전달
        teamService.deleteTeam(user.getUserId(), teamId);

        // 팀 삭제를 성공하면 HTTP 상태 코드 204(No Content) 및 성공 메시지를 반환
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(BaseResponseBody.of(0, "Success"));
    }

    @DeleteMapping("/{teamId}/{memberId}")
    @Operation(summary = "팀원 삭제", description = "팀장이 해당 유저를 삭제한다.")
    @ApiResponse(responseCode = "204", description = "성공 \n\n 'Success' 반환")
    public ResponseEntity<? extends BaseResponseBody> deleteMember(Authentication authentication,
                                                                   @PathVariable(name = "teamId") Long teamId,
                                                                   @PathVariable(name = "memberId") Long memberId){

        log.info("=======================Team Controller-deleteMember()=======================");

        // 현재 사용자의 세부 정보를 인증 객체에서 추출하여 저장
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 사용자, 팀, 추가 멤버 식별자 로깅
        log.info("userId : {}", user.getUserId());
        log.info("teamId : {}", teamId);
        log.info("memberId : {}", memberId);

        // 팀원 삭제 서비스 호출 및 사용자, 팀, 삭제 멤버 식별자 전달
        teamService.deleteMember(user.getUserId(), teamId, memberId);

        // 팀원 삭제를 성공하면 HTTP 상태 코드 204(No Content) 및 성공 메시지를 반환
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(BaseResponseBody.of(0, "Success"));
    }

    @GetMapping("/detail/{teamId}")
    @Operation(summary = "팀 상세 조회", description = "팀의 상세 정보를 본다")
    @ApiResponse(responseCode = "200", description = "성공 \n\n 팀 상세 정보 반환")
    public ResponseEntity<? extends BaseResponseBody> getTeamDetail(Authentication authentication,
                                                                    @PathVariable Long teamId){

        log.info("=======================Team Controller-getTeamDetail()=======================");

        // 현재 사용자의 세부 정보를 인증 객체에서 추출하여 저장
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 사용자, 팀 식별자 로깅
        log.info("userId : {}", user.getUserId());
        log.info("teamId : {}", teamId);

        // 팀 상세 조회 서비스 호출 및 사용자, 팀 식별자 전달
        TeamDetailDto teamDetailDto = teamService.getTeamDetail(user.getUserId(), teamId);

        // 조회가 성공하면 HTTP 상태 코드 200(OK) 및 팀의 상세 정보을 반환
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0,teamDetailDto));
    }

    @PatchMapping("/{teamId}")
    @Operation(summary = "팀 정보 수정", description = "팀장이 해당 팀의 정보를 수정한다.")
    @ApiResponse(responseCode = "200", description = "성공 \n\n 'Success' 반환")
    public ResponseEntity<? extends BaseResponseBody> modifyTeam(Authentication authentication,
                                                                 @PathVariable(name = "teamId") Long teamId,
                                                                 @RequestBody TeamModifyReqDto teamModifyReqDto){

        log.info("=======================Team Controller-modifyTeam()=======================");

        // 현재 사용자의 세부 정보를 인증 객체에서 추출하여 저장
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 사용자 식별자와 팀 정보 수정 요청 데이터 로깅
        log.info("userId : {}", user.getUserId());
        log.info("teamId : {}", teamId);
        log.info("teamName : {}", teamModifyReqDto.getName());
        log.info("teamDescription : {}", teamModifyReqDto.getDescription());

        // 팀 정보 수정 서비스 호출 및 사용자 식별자와 요청 데이터 전달
        teamService.modifyTeam(user.getUserId(), teamId, teamModifyReqDto);

        // 수정이 성공하면 HTTP 상태 코드 200(OK) 및 성공 메시지를 반환
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, "Success"));
    }

    @PatchMapping("/{teamId}/{memberId}")
    @Operation(summary = "리더 권한 변경", description = "팀장이 다른 팀원에게 리더를 양도한다.")
    @ApiResponse(responseCode = "200", description = "성공 \n\n 'Success' 반환")
    public ResponseEntity<? extends BaseResponseBody> modifyUserRole(Authentication authentication,
                                                                     @PathVariable Long teamId,
                                                                     @PathVariable Long memberId){

        log.info("=======================Team Controller-modifyUserRole()=======================");

        // 현재 사용자의 세부 정보를 인증 객체에서 추출하여 저장
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 사용자, 팀, 권한 양도 받을 멤버 식별자 로깅
        log.info("userId : {}", user.getUserId());
        log.info("teamId : {}", teamId);
        log.info("memberId : {}", memberId);

        // 리더 권한 변경 서비스 호출 및 사용자, 팀, 권한 양도 받을 멤버 식별자 전달
        teamService.modifyUserRole(user.getUserId(), teamId, memberId);

        // 권한 변경이 성공하면 HTTP 상태 코드 200(OK) 및 성공 메시지를 반환
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0,"Success"));
    }
}
