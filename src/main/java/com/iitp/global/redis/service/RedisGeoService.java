package com.iitp.global.redis.service;

import com.iitp.domains.map.dto.StoreLocationDto;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoRadiusCommandArgs;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisGeoService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String STORE_GEO_KEY = "store:geo";
    public static final String MEMBER_TEMPLATE_KEY = "tmp:user";

    /**
     * 가게 위치 정보를 Redis GEO에 추가 및 업데이트
     */
    public void updateStoreLocation(StoreLocationDto storeLocation) {
        try {
            Point point = new Point(storeLocation.longitude(), storeLocation.latitude());
            redisTemplate.opsForGeo().add(STORE_GEO_KEY, point, storeLocation.storeId().toString());
            log.debug("가게 위치 정보 Redis 저장 완료 - storeId: {}, lat: {}, lng: {}",
                    storeLocation.storeId(), storeLocation.latitude(), storeLocation.longitude());
        } catch (Exception e) {
            log.error("가게 위치 정보 Redis 저장 실패 - storeId: {}, error: {}",
                    storeLocation.storeId(), e.getMessage());
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
     * Store 엔티티로부터 직접 Redis Geo 업데이트
     */
    public void updateStoreLocationFromEntity(Store store) {
        if (store == null || store.getIsDeleted()) {
            log.warn("삭제된 상점이거나 null 상점입니다 - storeId: {}",
                    store != null ? store.getId() : "null");
            return;
        }

        StoreLocationDto storeLocation = StoreLocationDto.fromStore(store);
        updateStoreLocation(storeLocation);
    }

    /**
     * 가게 위치 정보 삭제
     */
    public void removeStoreLocation(Long storeId) {
        try {
            redisTemplate.opsForGeo().remove(STORE_GEO_KEY, storeId.toString());
            log.debug("가게 위치 정보 Redis 삭제 완료 - storeId: {}", storeId);
        } catch (Exception e) {
            log.error("가게 위치 정보 Redis 삭제 실패 - storeId: {}, error: {}", storeId, e.getMessage());
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

    public double getKiloMeterDistanceToStore(long storeId, double latitude, double longitude) {
        String tmpMember = MEMBER_TEMPLATE_KEY + UUID.randomUUID();
        GeoOperations<String, String> geo = redisTemplate.opsForGeo();

        geo.add(STORE_GEO_KEY, new Point(longitude, latitude), tmpMember); // (lon, lat)
        Distance distance = geo.distance(STORE_GEO_KEY, tmpMember, String.valueOf(storeId), Metrics.KILOMETERS);
        redisTemplate.opsForZSet().remove(STORE_GEO_KEY, tmpMember);
        return distance.getValue();
    }


}
