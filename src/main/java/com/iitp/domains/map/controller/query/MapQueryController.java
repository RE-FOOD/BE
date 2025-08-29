package com.iitp.domains.map.controller.query;

import com.iitp.domains.map.dto.responseDto.MapListResponseDto;
import com.iitp.domains.map.dto.responseDto.MapListScrollResponseDto;
import com.iitp.domains.map.dto.responseDto.MapMarkerResponseDto;
import com.iitp.domains.map.dto.responseDto.MapSummaryResponseDto;
import com.iitp.domains.map.service.query.MapQueryService;
import com.iitp.domains.store.domain.SortType;
import com.iitp.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maps")
@RequiredArgsConstructor
@Tag(name = "지도 API", description = "지도 관련 API")
public class MapQueryController {
    private final MapQueryService mapQueryService;

    @Operation(summary = "근처 가게 마커 조회")
    @GetMapping("/markers")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MapMarkerResponseDto>> getNearbyStoreMarkers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {

        List<MapMarkerResponseDto> markers = mapQueryService.getNearbyStoreMarkers(
                latitude, longitude, radiusKm);
        return ApiResponse.ok(200, markers, "근처 가게 마커 조회 성공");
    }

    @Operation(summary = "가게 지도 요약 조회")
    @GetMapping("/{storeId}/summary")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MapSummaryResponseDto> getStoreSummary(
            @PathVariable Long storeId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        MapSummaryResponseDto summary = mapQueryService.getStoreSummary(
                storeId, latitude, longitude);
        return ApiResponse.ok(200, summary, "가게 요약 정보 조회 성공");
    }

    @Operation(summary = "가게 지도 목록 조회")
    @GetMapping("/lists")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MapListScrollResponseDto> getNearbyStoreList(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm,
            @RequestParam(defaultValue = "NEAR") SortType sort,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") Integer limit) {

        MapListScrollResponseDto stores = mapQueryService.getNearbyStoreListWithScroll(
                latitude, longitude, radiusKm, sort, cursorId, limit);
        return ApiResponse.ok(200, stores, "근처 가게 목록 조회 성공");
    }
}