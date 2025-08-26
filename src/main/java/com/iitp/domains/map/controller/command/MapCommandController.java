package com.iitp.domains.map.controller.command;

import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.redis.service.RedisGeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maps")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "지도 API", description = "지도 관련 API")
public class MapCommandController {
    private final RedisGeoService redisGeoService;

    @Operation(summary = "전체 Redis Geo 데이터 동기화",
            description = "DB의 모든 상점 데이터를 Redis Geo에 일괄 동기화합니다.")
    @PostMapping("/redisgeo/syn")
    public ApiResponse<String> syncAllStoreLocations() {
        log.info("전체 Redis Geo 데이터 동기화 요청");
        redisGeoService.syncAllStoreLocations();

        return ApiResponse.ok(200, null, "Redis Geo 동기화 성공");
    }
}
