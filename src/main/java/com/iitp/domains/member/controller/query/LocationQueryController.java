package com.iitp.domains.member.controller.query;

import com.iitp.domains.member.dto.responseDto.LocationResponseDto;
import com.iitp.domains.member.service.query.LocationQueryService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.config.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "주소 조회", description = "회원 주소 조회 API")
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
@RestController
@Slf4j
public class LocationQueryController {
    private final LocationQueryService locationQueryService;

    @Operation(summary = "주소 목록 조회",
            description = "현재 로그인한 회원의 모든 주소를 조회합니다.")
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<LocationResponseDto>> getMyAddresses() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        log.info("주소 목록 조회 - memberId: {}", memberId);

        List<LocationResponseDto> addresses = locationQueryService.findMemberAddresses(memberId);

        return ApiResponse.ok(200, addresses, "주소 목록 조회 성공");
    }

    @Operation(summary = "현재 기본 주소 조회",
            description = "현재 설정된 기본 주소만 조회합니다.")
    @GetMapping("/default")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<LocationResponseDto> getDefaultAddress() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        log.info("기본 주소 조회 - memberId: {}", memberId);

        LocationResponseDto defaultAddress = locationQueryService.findDefaultAddress(memberId);

        return ApiResponse.ok(200, defaultAddress, "기본 주소 조회 성공");
    }
}
