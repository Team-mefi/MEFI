package com.mefi.backend.api.controller;

import com.mefi.backend.api.response.NotiResponseDto;
import com.mefi.backend.api.service.NotiService;
import com.mefi.backend.common.auth.CustomUserDetails;
import com.mefi.backend.common.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
@Tag(name = "알림 API", description = "SSE 방식을 이용해 사용자에게 필요한 알림을 전송하는 컨트롤러")
@Slf4j
public class NotiController {

    private final NotiService notiService;

    @GetMapping("/subscribe")
    @Operation(summary = "SSE 연결 API", description = "서버에서 클라이언트에 실시간 알림을 전송하기 위한 SSE Emitter 생성")
    public ResponseEntity<SseEmitter> createSseConnection( Authentication authentication,  @RequestParam(name = "lastEventId", required = false) String lastEventId){

        // 현재 사용자의 식별 ID 조회
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        log.info("User ID : {}", user.getUserId());

        // SSE Emitter 생성
        SseEmitter sseEmitter = notiService.createSseConnection(user.getUserId(), lastEventId);
        log.info("SSE Emitter : {}", sseEmitter);
        return ResponseEntity.status(HttpStatus.CREATED).body(sseEmitter);
    }

    @GetMapping("/all")
    @Operation(summary = "알림 전체 조회 API", description = "사용자가 읽지 않은 모든 알림을 조회합니다")
    public ResponseEntity<? extends BaseResponseBody> getNotis(Authentication authentication){
        // 현재 사용자의 식별 ID 조회
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        log.info("User ID : {}", user.getUserId());

        // 사용자가 읽지 않은 모든 알림 조회
        List<NotiResponseDto> notiList = notiService.getNotis(user.getUserId());
        log.info(notiList.toString());
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, notiList));
    }

    @PatchMapping("/{alarmId}")
    @Operation(summary = "알림 읽음 API", description = "사용자가 읽지 않은 특정 알림을 읽음 처리합니다.")
    public ResponseEntity<? extends BaseResponseBody> readNoti(Authentication authentication,  @PathVariable(name = "alarmId") Long alarmId){
         // 현재 사용자의 식별 ID 조회
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        log.info("User ID : {}", user.getUserId());

        // 특정 알림에 대해 읽음 처리
        NotiResponseDto noti = notiService.readNoti(alarmId);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, noti));
    }

    @PatchMapping("/all")
    @Operation(summary = "알림 전체 읽음 API", description = "사용자가 읽지 않은 모든 알림을 읽음 처리합니다.")
    public ResponseEntity<? extends BaseResponseBody> readNotiAll(Authentication authentication){
        // 현재 사용자의 식별 ID 조회
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        log.info("User ID : {}", user.getUserId());

        // 모든 알림에 대해 읽음 처리
        int count = notiService.readNotiAll(user.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, count));
    }
}
