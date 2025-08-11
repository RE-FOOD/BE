package com.iitp.domains.auth.controller.command;

import com.iitp.domains.auth.dto.responseDto.*;
import com.iitp.domains.auth.service.command.AuthCommandService;
import com.iitp.domains.auth.dto.requestDto.MemberLogInRequestDto;
import com.iitp.domains.auth.dto.requestDto.MemberSignupRequestDto;
import com.iitp.domains.auth.dto.requestDto.StoreSignupRequestDto;
import com.iitp.domains.auth.dto.requestDto.TokenRefreshRequestDto;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.config.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.token.TokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증 관리", description = "회원가입, 로그인, 로그아웃, 토큰 갱신 API")
public class AuthCommandControoler {
    private final AuthCommandService authCommandService;

    // 개인 회원가입
    @Operation(summary = "개인 회원가입",
            description = "카카오 소셜 로그인을 통한 회원가입을 진행합니다.")
    @PostMapping("/signup/members")
    public ApiResponse<MemberSignupResponseDto> signupMember(
            @Valid @RequestBody MemberSignupRequestDto request) {
        MemberSignupResponseDto response = authCommandService.memberSignup(request);
        return ApiResponse.ok(201, response, "개인 회원가입 완료");
    }

    // 사업자 회원가입
    @Operation(summary = "사업자 회원가입",
            description = "카카오 소셜 로그인을 통한 회원가입을 진행합니다.")
    @PostMapping("/signup/stores")
    public ApiResponse<StoreSignupResponseDto> signupStore(
            @Valid @RequestBody StoreSignupRequestDto request) {
        StoreSignupResponseDto response = authCommandService.signupStore(request);
        return ApiResponse.ok(201, response, "사업자 회원가입 완료");
    }


    @Operation(summary = "로그인", description = "카카오 소셜 로그인을 통한 로그인을 진행합니다.")
    @PostMapping("/login/members")
    public ApiResponse<LoginResponseDto> signin(
            @Valid @RequestBody MemberLogInRequestDto request) {
        LoginResponseDto response = authCommandService.signin(request);
        return ApiResponse.ok(200, response,"로그인 완료");
    }

    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자를 로그아웃합니다.")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> signout() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        authCommandService.signout(memberId);
        return ApiResponse.okWithoutData(200,"로그아웃 되었습니다.");
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.")
    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponseDto> refreshToken(
            @Valid @RequestBody TokenRefreshRequestDto request) {

        TokenRefreshResponseDto response = authCommandService.refreshToken(request.refreshToken());
        return ApiResponse.ok(200, response, "토큰 갱신 완료");
    }
}
