package com.mefi.backend.api.controller;

import com.mefi.backend.api.request.JoinReqDto;
import com.mefi.backend.api.request.VerifyCodeReqDto;
import com.mefi.backend.api.request.VerifyEmailReqDto;
import com.mefi.backend.api.service.MailServiceImpl;
import com.mefi.backend.api.service.TokenService;
import com.mefi.backend.api.service.UserService;
import com.mefi.backend.common.auth.CustomUserDetails;
import com.mefi.backend.common.model.BaseResponseBody;
import com.mefi.backend.db.entity.Token;
import com.mefi.backend.db.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name="1.USER", description="USER API")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final MailServiceImpl mailService;

    @Operation(summary = "회원가입", description = "/users\n\n 사용자의 정보를 통해 회원가입 한다.")
    @PostMapping("")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> join(@RequestBody JoinReqDto joinReqDto) {

        // 회원가입
        userService.join(joinReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseBody.of(0,"Success"));
    }

    @Operation(summary = "회원탈퇴", description = "/users\n\n 사용자는 회원탈퇴를 한다.")
    @DeleteMapping("")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> withdraw(Authentication authentication) {

        // 로그인된 유저 정보 조회
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 유저 식별 ID로 유저 정보 조회
        User user = userService.findById(userDetails.getUserId());

        // 유저 제거
        userService.withdraw(user);

        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0,"Success"));
    }

    @Operation(summary = "로그아웃", description = "/users/logout\n\n 사용자는 로그아웃을 한다.")
    @PostMapping("/logout")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> logout(Authentication authentication) {

        // 로그인된 유저 정보 조회
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 식별 ID로 리프레시 토큰 제거
        Token token = tokenService.findByUserId(userDetails.getUserId());
        tokenService.deleteRefreshToken(token);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0,"Success"));
    }

    @Operation(summary = "이메일 인증", description = "/users/regist/{userId}/auth\n\n 사용자는 이메일 인증을 한다.")
    @PostMapping("/regist/auth")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> verifyEmail(@RequestBody VerifyEmailReqDto verifyEmailReqDto) throws Exception {

        // 메일 전송 후 코드 받기
        mailService.sendMessage(verifyEmailReqDto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseBody.of(0,"Success"));
    }

    @Operation(summary = "이메일 인증 확인", description = "/users/regist/auth/check\n\n 사용자는 이메일 인증 확인을 한다.")
    @PostMapping("/regist/auth/check")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Token 반환")
    public ResponseEntity<? extends BaseResponseBody> verifyEmailCode(@RequestBody VerifyCodeReqDto verifyCodeReqDto) {

        // 인증 코드 확인 후 토큰 반환
        String token = mailService.validateAuthCode(verifyCodeReqDto);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0,token));
    }
}