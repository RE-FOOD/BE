package com.iitp.domains.member.controller.command;

import com.iitp.domains.member.dto.requestDto.MemberUpdateNicknameRequestDto;
import com.iitp.domains.member.dto.responseDto.MemberUpdateNicknameResponseDto;
import com.iitp.domains.auth.dto.requestDto.FcmTokenUpdateRequestDto;
import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.dto.requestDto.LocationCreateRequestDto;
import com.iitp.domains.member.dto.responseDto.LocationResponseDto;
import com.iitp.global.config.security.SecurityUtil;
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
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증 관리", description = "회원가입, 로그인,닉네임 수정, 로그아웃 API")
public class MemberCommandController {
    private final MemberCommandService memberCommandService;

    // 회원 탈퇴
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 회원을 탈퇴 처리합니다.")
    @PatchMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> deleteMember() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        memberCommandService.deleteMember(memberId);
        return ApiResponse.okWithoutData(200, "회원 탈퇴 완료");
    }

    @Operation(summary = "닉네임 수정", description = "현재 로그인한 회원의 닉네임을 수정합니다.")
    @PatchMapping("/nickname")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MemberUpdateNicknameResponseDto> updateNickname(
            @Valid @RequestBody MemberUpdateNicknameRequestDto request) {

        Long memberId = SecurityUtil.getCurrentMemberId();
        MemberUpdateNicknameResponseDto response = memberCommandService.updateNickname(memberId, request);
        return ApiResponse.ok(200, response, "닉네임 수정 완료");
    }

    // 새 위치 추가
    @Operation(summary = "새 위치 추가",
            description = "현재 로그인한 회원의 새로운 위치를 추가합니다.")
    @PostMapping("/location")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<LocationResponseDto> addLocation(
            @Valid @RequestBody LocationCreateRequestDto request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Location location = memberCommandService.addNewLocation(memberId, request.address(), true);

        LocationResponseDto response = LocationResponseDto.from(location);

        return ApiResponse.ok(200, response, "위치 추가 완료");
    }
}
