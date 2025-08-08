package com.iitp.domains.member.controller.command;

import com.iitp.global.config.security.SecurityUtil;
import com.iitp.domains.member.dto.requestDto.MemberLogInRequestDto;
import com.iitp.domains.member.dto.requestDto.MemberSignupRequestDto;
import com.iitp.domains.member.dto.requestDto.StoreSignupRequestDto;
import com.iitp.domains.member.dto.responseDto.MemberLogInResponseDto;
import com.iitp.domains.member.dto.responseDto.MemberSignupResponseDto;
import com.iitp.domains.member.dto.responseDto.StoreSignupResponseDto;
import com.iitp.domains.member.service.command.MemberCommandService;
import com.iitp.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증 관리", description = "회원가입, 로그인, 로그아웃 API")
public class MemberCommandController {
    private final MemberCommandService memberCommandService;

    // 개인 회원가입
    @Operation(summary = "개인 회원가입",
            description = "카카오 소셜 로그인을 통한 회원가입을 진행합니다.")
    @PostMapping("/signup/member")
    public ApiResponse<MemberSignupResponseDto> signupMember(
            @Valid @RequestBody MemberSignupRequestDto request) {
        MemberSignupResponseDto response = memberCommandService.memberSignup(request);
        return ApiResponse.ok(201, response, "개인 회원가입 완료");
    }

    // 사업자 회원가입
    @Operation(summary = "사업자 회원가입",
            description = "카카오 소셜 로그인을 통한 회원가입을 진행합니다.")
    @PostMapping("/signup/store")
    public ApiResponse<StoreSignupResponseDto> signupStore(
            @Valid @RequestBody StoreSignupRequestDto request) {
        StoreSignupResponseDto response = memberCommandService.signupStore(request);
        return ApiResponse.ok(201, response, "사업자 회원가입 완료");
    }


    @Operation(summary = "로그인", description = "카카오 소셜 로그인을 통한 로그인을 진행합니다.")
    @PostMapping("/login/member")
    public ApiResponse<MemberLogInResponseDto> signin(
            @Valid @RequestBody MemberLogInRequestDto request) {
        MemberLogInResponseDto response = memberCommandService.signin(request);
        return ApiResponse.ok(200, response,"로그인 완료");
    }

    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자를 로그아웃합니다.")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> signout() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        memberCommandService.signout(memberId);
        return ApiResponse.okWithoutData(200,"로그아웃 되었습니다.");
    }

    @Operation(summary = "새 위치 추가", description = "현재 로그인한 회원의 새로운 위치를 추가합니다.")
    @PostMapping("/location")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> addLocation(
            @Parameter(description = "새로운 주소")
            @RequestParam String address) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        memberCommandService.addNewLocation(memberId, address, true);
        return ApiResponse.okWithoutData(200, "위치 추가 완료");
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 회원을 탈퇴 처리합니다.")
    @PatchMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> deleteMember() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        memberCommandService.deleteMember(memberId);
        return ApiResponse.okWithoutData(200, "회원 탈퇴 완료");
    }
}
