package com.mefi.backend.api.controller;

import com.mefi.backend.api.request.*;
import com.mefi.backend.api.response.MemberResDto;
import com.mefi.backend.api.service.MailServiceImpl;
import com.mefi.backend.api.service.TokenService;
import com.mefi.backend.api.service.UserService;
import com.mefi.backend.common.auth.CustomUserDetails;
import com.mefi.backend.common.model.BaseResponseBody;
import com.mefi.backend.db.entity.Token;
import com.mefi.backend.db.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name="1.USER", description="USER API")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final MailServiceImpl mailService;

    @Operation(summary = "회원가입", description = "/users\n\n 사용자의 정보를 통해 회원가입 한다.")
    @PostMapping("")
    @ApiResponse(responseCode = "201", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> join(@RequestBody JoinReqDto joinReqDto) {

        // 회원가입
        userService.join(joinReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseBody.of(0, "Success"));
    }

    @Operation(summary = "회원탈퇴", description = "/users\n\n 사용자는 회원탈퇴를 한다.")
    @DeleteMapping("")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> withdraw(Authentication authentication,
                                                               @RequestBody UserWithdrawReqDto userWithdrawReqDto) {

        // 로그인된 유저 정보 조회
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 유저 식별 ID로 유저 정보 조회
        User user = userService.findById(userDetails.getUserId());

        // 유저 제거
        userService.withdraw(user, userWithdrawReqDto);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, "Success"));
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
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, "Success"));
    }

    @Operation(summary = "이메일 인증", description = "/users/join/auth\n\n 사용자는 이메일 인증을 한다.")
    @PostMapping("/join/auth")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> verifyEmail(@RequestBody VerifyEmailReqDto verifyEmailReqDto) throws Exception {

        // 메일 전송 후 코드 받기
        mailService.sendMessage(verifyEmailReqDto.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, "Success"));
    }

    @Operation(summary = "이메일 인증 확인", description = "/users/join/auth/check\n\n 사용자는 이메일 인증 확인을 한다.")
    @PostMapping("/join/auth/check")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Token 반환")
    public ResponseEntity<? extends BaseResponseBody> verifyEmailCode(@RequestBody VerifyCodeReqDto verifyCodeReqDto) {

        // 인증 코드 확인 후 토큰 반환
        String token = mailService.validateAuthCode(verifyCodeReqDto);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, token));
    }

    @Operation(summary = "회원 검색", description = "/users/search/{keyword} \n\n 사용자는 회원을 검색한다.")
    @GetMapping("/search/{keyword}")
    @ApiResponse(responseCode = "200", description = "성공 \n\n 검색 결과 유저 리스트 반환")
    public ResponseEntity<? extends BaseResponseBody> searchUsers(
            @Parameter(name = "keyword", description = "검색 키워드")
            @PathVariable("keyword") String keyword) {

        // 검색어로 회원 조회
        List<MemberResDto> searchResultList = userService.getSearchUsers(keyword);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, searchResultList));
    }

    @Operation(summary = "회원 정보 수정", description = "/users/info \n\n 사용자는 자신의 정보를 수정한다.")
    @PatchMapping("/info")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> modifyUserInfo(Authentication authentication,
                                                                     @RequestBody UserModifyReqDto userModifyReqDto) {

        // 로그인된 유저 정보 조회
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 회원 정보 수정
        userService.modifyUserInfo(userDetails.getUserId(), userModifyReqDto);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, "Success"));
    }

    @Operation(summary = "비밀번호 수정", description = "/users/pwd \n\n 사용자는 비밀번호를 수정한다.")
    @PatchMapping("/pwd")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Success 반환")
    ResponseEntity<? extends BaseResponseBody> modifyUserPassword(@RequestBody UserModifyPasswordReqDto userModifyPasswordReqDto) {

        // 회원 비밀번호 수정
        userService.modifyUserPassword(userModifyPasswordReqDto);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, "Success"));
    }
}