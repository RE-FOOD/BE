package com.iitp.global.redis.service;

import com.iitp.domains.map.dto.StoreLocationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisGeoService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String STORE_GEO_KEY = "store:geo";

    /**
     * 가게 위치 정보를 Redis GEO에 추가
     */
    public void addStoreLocation(Long storeId, Double latitude, Double longitude) {
        try {
            Point point = new Point(longitude, latitude);
            redisTemplate.opsForGeo().add(STORE_GEO_KEY, point, storeId.toString());
            log.debug("가게 위치 정보 Redis 저장 완료 - storeId: {}, lat: {}, lng: {}",
                    storeId, latitude, longitude);
        } catch (Exception e) {
            log.error("가게 위치 정보 Redis 저장 실패 - storeId: {}, error: {}", storeId, e.getMessage());
        }
    }

    /**
     * 여러 가게 위치 정보를 한 번에 추가
     */
    public void addStoreLocations(List<StoreLocationDto> storeLocations) {
        try {
            List<RedisGeoCommands.GeoLocation<String>> geoLocations = storeLocations.stream()
                    .map(store -> new RedisGeoCommands.GeoLocation<>(
                            store.storeId().toString(),
                            new Point(store.longitude(), store.latitude())
                    ))
                    .collect(Collectors.toList());

            redisTemplate.opsForGeo().add(STORE_GEO_KEY, geoLocations);
            log.info("여러 가게 위치 정보 Redis 저장 완료 - 개수: {}", storeLocations.size());
        } catch (Exception e) {
            log.error("여러 가게 위치 정보 Redis 저장 실패 - error: {}", e.getMessage());
        }
    }

    /**
     * 특정 위치 근처의 가게 ID 조회
     */
    public List<String> findNearbyStores(Double latitude, Double longitude, Double radiusKm) {
        try {
            Point center = new Point(longitude, latitude);
            Distance radius = new Distance(radiusKm, Metrics.KILOMETERS);
            Circle circle = new Circle(center, radius);

            GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                    redisTemplate.opsForGeo().radius(STORE_GEO_KEY, circle);

            List<String> storeIds = results.getContent().stream()
                    .map(result -> result.getContent().getName())
                    .collect(Collectors.toList());

            log.debug("근처 가게 조회 완료 - 중심: ({}, {}), 반경: {}km, 결과: {}개",
                    latitude, longitude, radiusKm, storeIds.size());

            return storeIds;
        } catch (Exception e) {
            log.error("근처 가게 조회 실패 - lat: {}, lng: {}, radius: {}km, error: {}",
                    latitude, longitude, radiusKm, e.getMessage());
            return List.of();
        }
    }

    /**
     * 모든 가게 위치 정보 삭제 (초기화용)
     */
    public void clearAllStoreLocations() {
        try {
            redisTemplate.delete(STORE_GEO_KEY);
            log.info("모든 가게 위치 정보 Redis 삭제 완료");
        } catch (Exception e) {
            log.error("모든 가게 위치 정보 Redis 삭제 실패 - error: {}", e.getMessage());
        }
    }

}
