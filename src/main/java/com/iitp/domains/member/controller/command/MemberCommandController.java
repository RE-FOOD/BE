package com.iitp.domains.member.controller.command;

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
@Tag(name = "인증 관리", description = "회원가입, 로그인, 로그아웃 API")
public class MemberCommandController {
    private final MemberCommandService memberCommandService;

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 회원을 탈퇴 처리합니다.")
    @PatchMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> deleteMember() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        memberCommandService.deleteMember(memberId);
        return ApiResponse.okWithoutData(200, "회원 탈퇴 완료");
    }

    @Operation(summary = "새 위치 추가",
            description = "현재 로그인한 회원의 새로운 위치를 추가합니다.")
    @PostMapping("/location")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<LocationResponseDto> addLocation(
            @Valid @RequestBody LocationCreateRequestDto request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Location location = memberCommandService.addNewLocation(memberId, request.address(), true);

        LocationResponseDto response = LocationResponseDto.of(
                location.getId(),
                location.getAddress(),
                location.getIsMostRecent()
        );
        return ApiResponse.ok(200, response, "위치 추가 완료");
    }
    @Operation(summary = "FCM 토큰 업데이트",
            description = "푸시 알림을 위한 FCM 토큰을 업데이트합니다.")
    @PatchMapping("/fcmToken")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> updateFcmToken(
            @Valid @RequestBody FcmTokenUpdateRequestDto request) {

        Long memberId = SecurityUtil.getCurrentMemberId();
        memberCommandService.updateFcmToken(memberId, request.fcmToken());

        return ApiResponse.okWithoutData(200, "FCM 토큰 업데이트 완료");
    }

    /**
     * FCM 토큰 삭제 (푸시 알림 비활성화)
     */
    @Operation(summary = "FCM 토큰 삭제",
            description = "푸시 알림을 비활성화합니다.")
    @DeleteMapping("/fcmToken")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> removeFcmToken() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        memberCommandService.removeFcmToken(memberId);

        return ApiResponse.okWithoutData(200, "푸시 알림이 비활성화되었습니다");
    }
}
