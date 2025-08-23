package com.iitp.domains.member.controller.command;

import com.iitp.domains.member.dto.requestDto.LocationCreateRequestDto;
import com.iitp.domains.member.dto.responseDto.LocationResponseDto;
import com.iitp.domains.member.service.command.LocationCommandService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.config.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "주소 관리", description = "회원 주소 관리 API")
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
@RestController
@Slf4j
public class LocationCommandController {

    private final LocationCommandService locationCommandService;

    @Operation(summary = "새 주소 추가",
            description = "새로운 주소를 등록합니다. 등록된 주소는 자동으로 기본 주소로 설정됩니다.")
    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<LocationResponseDto> addAddress(
            @Valid @RequestBody LocationCreateRequestDto request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        log.info("주소 추가 - memberId: {}, fullAddress: {}", memberId, request.address());

        LocationResponseDto response = locationCommandService.addNewAddress(memberId, request);

        return ApiResponse.ok(201, response, "주소 추가 성공");
    }

    @Operation(summary = "기본 주소 변경",
            description = "특정 주소를 기본 주소로 설정합니다.")
    @PatchMapping("/{addressId}/setDefault")
    public ApiResponse<LocationResponseDto> setDefaultAddress(
            @Parameter(description = "기본 주소로 설정할 주소 ID", required = true)
            @PathVariable Long addressId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        log.info("기본 주소 변경 - memberId: {}, addressId: {}", memberId, addressId);

        LocationResponseDto response = locationCommandService.setDefaultAddress(memberId, addressId);

        return ApiResponse.ok(200, response, "기본 주소 변경 성공");
    }

    @Operation(summary = "주소 삭제",
            description = "특정 주소를 삭제합니다. 기본 주소가 삭제될 경우, 가장 최근에 등록된 주소가 자동으로 기본 주소로 설정됩니다.")
    @DeleteMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> deleteAddress(
            @Parameter(description = "삭제할 주소 ID", required = true)
            @PathVariable Long addressId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        log.info("주소 삭제 - memberId: {}, addressId: {}", memberId, addressId);

        locationCommandService.deleteAddress(memberId, addressId);

        return ApiResponse.okWithoutData(200, "주소 삭제 성공");
    }
}
