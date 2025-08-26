package com.iitp.global.redis.service;

import com.iitp.domains.map.dto.StoreLocationDto;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.store.StoreRepository;
import java.util.LinkedHashMap;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisGeoInitService implements ApplicationRunner {
    private final RedisGeoService redisGeoService;
    private final StoreRepository storeRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeStoreLocations();
    }

    /**
     * 애플리케이션 시작 시 DB의 모든 가게 위치 정보를 Redis GEO에 저장
     */
    public void initializeStoreLocations() {
        try {
            log.info("가게 위치 정보 Redis 초기화 시작");

            // 기존 데이터 삭제
            redisGeoService.clearAllStoreLocations();

            // DB에서 모든 활성 가게 조회
            List<Store> stores = storeRepository.findAll().stream()
                    .filter(store -> !store.getIsDeleted())
                    .filter(store -> store.getLatitude() != null && store.getLongitude() != null)
                    .collect(Collectors.toList());

            if (stores.isEmpty()) {
                log.warn("저장할 가게 위치 정보가 없습니다.");
                return;
            }

            // Redis GEO에 저장할 데이터 변환
            List<StoreLocationDto> storeLocations = stores.stream()
                    .map(StoreLocationDto::fromStore)
                    .collect(Collectors.toList());

            // 배치로 Redis에 저장
            redisGeoService.addStoreLocations(storeLocations);

            log.info("가게 위치 정보 Redis 초기화 완료 - 총 {}개 가게", storeLocations.size());

        } catch (Exception e) {
            log.error("가게 위치 정보 Redis 초기화 실패", e);
        }
    }

    /**
     * 수동으로 초기화 실행 (관리용)
     */
    public void manualInitialize() {
        initializeStoreLocations();
    }
}