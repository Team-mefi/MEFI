package com.mefi.backend.api.controller;

import com.mefi.backend.api.request.ScheduleReqDto;
import com.mefi.backend.api.response.ScheduleDetailResDto;
import com.mefi.backend.api.response.ScheduleResDto;
import com.mefi.backend.api.response.ScheduleTimeDto;
import com.mefi.backend.api.service.ScheduleService;
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
@RequestMapping("api/schedule")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "개인 일정 API", description = "각 팀원은 개인적인 일정을 추가, 수정, 삭제할 수 있다.")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @Operation(summary = "개인 일정 등록", description = "새로운 개인 일정 정보를 받아 DB에 저장한다.")
    @ApiResponse(responseCode = "201", description = "성공 시 상태 코드 201와 SUCCESS 반환")
    public ResponseEntity<? extends BaseResponseBody> createSchedule(Authentication authentication, @RequestBody ScheduleReqDto scheduleReqDto){
        // 로그인 된 유저 정보 조회
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        log.info("User ID : {}", user.getUserId());

        // 일정 등록
        scheduleService.createSchedule(user.getUserId(), scheduleReqDto);
        log.info("Schedule Summary : {}", scheduleReqDto.getSummary());

        // 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseBody.of(0, "SUCCESS"));
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "개인 일정 삭제", description = "개인 일정을 DB에서 삭제한다")
    @ApiResponse(responseCode = "204", description = "")
    public ResponseEntity<? extends BaseResponseBody> deleteSchedule(Authentication authentication, @PathVariable("scheduleId") Long scheduleId){
        // 로그인 된 유저 정보 조회
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 일정 삭제
        ScheduleResDto scheduleResDto = scheduleService.deleteSchedule(user.getUserId(), scheduleId);

        // 반환
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(BaseResponseBody.of(0, "SUCCESS"));
    }

    @GetMapping
    @Operation(summary = "기간 내 개인 일정 조회", description = "사용자의 모든 개인 일정 정보를 조회한다.")
    @ApiResponse(responseCode = "200", description = "성공 시 상태 코드 200와 개인 일정 리스트 반환")
    public ResponseEntity<? extends BaseResponseBody> getPrivateSchedule(Authentication authentication,
                                                                         @RequestParam(name = "start") String start,
                                                                         @RequestParam(name = "end") String end){

        log.info("=======================ScheduleController-getPrivateSchedule()=======================");

        // 현재 사용자의 세부 정보를 인증 객체에서 추출하여 저장
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 사용자 식별자와 조회 일자 로깅
        log.info("userID : {} ", user.getUserId());
        log.info("start : {} ", start);
        log.info("end : {} ", end);

        // 개인 일정 조회 서비스 호출 및 사용자 식별자, 조회 기간 전달
        List<ScheduleDetailResDto> schedule = scheduleService.getPrivateSchedule(user.getUserId(), start, end);

        // 조회가 성공하면 HTTP 상태 코드 200(OK) 및 개인 일정 정보을 반환
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, schedule));
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "해당 일자 팀원 전체 일정 조회", description = "리더가 자신 포함 모든 멤버의 해당 일자 일정 정보를 조회한다.")
    @ApiResponse(responseCode = "200", description = "성공 시 상태 코드 200와 모든 멤버의 해당 일자 일정 정보")
    public ResponseEntity<? extends BaseResponseBody> getAllMemberSchedule(Authentication authentication,
                                                                           @PathVariable("teamId") Long teamId,
                                                                           @RequestParam(name = "day") String day){

        log.info("=======================ScheduleController-getAllMemberSchedule()=======================");

        // 현재 사용자의 세부 정보를 인증 객체에서 추출하여 저장
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 사용자, 팀 식별자와 조회 일자 로깅
        log.info("userId : {} ", user.getUserId());
        log.info("teamId : {} ", teamId);
        log.info("start : {} ", day);

        // 팀원 전체 일정 조회 서비스 호출 및 사용자, 팀 식별자와 조회 일자 전달
        List<ScheduleTimeDto> result = scheduleService.getAllMemberSchedule(user.getUserId(), teamId, day);

        // 조회가 성공하면 HTTP 상태 코드 200(OK) 및 팀원 전체 일정 정보을 반환
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, result));
    }

    @PatchMapping("/{scheduleId}")
    @Operation(summary = "개인 일정 수정", description = "새로운 개인 일정 정보를 받아 DB에 수정한다.")
    @ApiResponse(responseCode = "200", description = "성공 시 상태 코드 200와 SUCCESS 반환")
    public ResponseEntity<? extends BaseResponseBody> modifySchedule(Authentication authentication,
                                                                     @RequestBody ScheduleReqDto scheduleReqDto,
                                                                     @PathVariable(name = "scheduleId") Long scheduleId){

        log.info("=======================ScheduleController-modifySchedule()=======================");

        // 현재 사용자의 세부 정보를 인증 객체에서 추출하여 저장
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        // 사용자, 수정 일정 식별자 로깅
        log.info("User ID : {}", user.getUserId());
        log.info("Schedule ID : {}", scheduleId);

        // 개인 일정 수정 서비스 호출 및 사용자, 사용자, 수정 일정 식별자와 수정 데이터 전달
        scheduleService.modifySchedule(user.getUserId(), scheduleReqDto, scheduleId);

        // 수정이 성공하면 HTTP 상태 코드 200(OK) 및 성공 메시지를 반환
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, "SUCCESS"));
    }
}
