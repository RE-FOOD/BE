package com.iitp.domains.member.controller.command;

import com.iitp.domains.member.config.security.SecurityUtil;
import com.iitp.domains.member.dto.requestDto.MemberLogInRequestDto;
import com.iitp.domains.member.dto.requestDto.MemberSignupRequestDto;
import com.iitp.domains.member.dto.requestDto.StoreSignupRequestDto;
import com.iitp.domains.member.service.command.MemberCreateService;
import com.iitp.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증 관리", description = "회원가입, 로그인, 로그아웃 API")
public class MemberController {
    private final MemberCreateService memberCreateService;

    // 개인 회원가입
    @Operation(summary = "개인 회원가입",
            description = "카카오 소셜 로그인을 통한 회원가입을 진행합니다.")
    @PostMapping("/signup/member")
    public ResponseEntity<ApiResponse<Object>> signupMember(
            @Valid @RequestBody MemberSignupRequestDto request) {
        var response = memberCreateService.memberSignup(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "개인 회원가입 완료"));
    }

    // 사업자 회원가입
    @Operation(summary = "사업자 회원가입",
            description = "카카오 소셜 로그인을 통한 회원가입을 진행합니다.")
    @PostMapping("/signup/store")
    public ResponseEntity<ApiResponse<Object>> signupStore(
            @Valid @RequestBody StoreSignupRequestDto request) {
        var response = memberCreateService.signupStore(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "사업자 회원가입 완료"));
    }


    @Operation(summary = "로그인", description = "카카오 소셜 로그인을 통한 로그인을 진행합니다.")
    @PostMapping("/login/member")
    public ResponseEntity<ApiResponse<Object>> signin(@Valid @RequestBody MemberLogInRequestDto request) {
        var response = memberCreateService.signin(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "로그인 완료"));
    }

    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자를 로그아웃합니다.")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> signout() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        memberCreateService.signout(memberId);
        return ResponseEntity.ok(ApiResponse.okWithoutData(200, "로그아웃 완료"));
    }

    @Operation(summary = "새 위치 추가", description = "현재 로그인한 회원의 새로운 위치를 추가합니다.")
    @PostMapping("/location")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> addLocation(
            @Parameter(description = "새로운 주소")
            @RequestParam String address) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        memberCreateService.addNewLocation(memberId, address, true);
        return ResponseEntity.ok(ApiResponse.okWithoutData(200, "위치 추가 완료"));
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 회원을 탈퇴 처리합니다.")
    @PatchMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> deleteMember() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        memberCreateService.deleteMember(memberId);
        return ResponseEntity.ok(ApiResponse.okWithoutData(200, "회원 탈퇴 완료"));
    }
}
