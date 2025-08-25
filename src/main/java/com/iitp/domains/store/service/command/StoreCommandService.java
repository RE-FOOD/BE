package com.iitp.domains.store.service.command;

import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.domain.entity.StoreImage;
import com.iitp.domains.store.dto.request.StoreCreateRequest;
import com.iitp.domains.store.dto.request.StoreUpdateRequest;
import com.iitp.domains.store.repository.store.StoreImageRepository;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.geoCode.GeocodingResult;
import com.iitp.global.geoCode.KakaoGeocodingService;
import com.iitp.global.redis.service.RedisGeoService;
import com.iitp.global.redis.service.StoreRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StoreCommandService {
    private final KakaoGeocodingService  kakaoGeocodingService;
    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;
    private final StoreRedisService cacheService;
    private final RedisGeoService redisGeoService;


    public Long createStore(StoreCreateRequest request, Long userId) {
        // 주소로 위/경도 조회
        GeocodingResult geocodingResult = kakaoGeocodingService.getCoordinates(request.address());

        Store store = request.toEntity(userId, geocodingResult.latitude(), geocodingResult.longitude());

        storeRepository.save(store);

        for(String img : request.imageKey()){
             storeImageRepository.save(new StoreImage(img, store));
        }
        // Redis Geo에 상점 위치 정보 추가
        try {
            redisGeoService.updateStoreLocationFromEntity(store);
            log.info("상점 생성 완료 및 Redis Geo 업데이트 - storeId: {}", store.getId());
        } catch (Exception e) {
            log.warn("Redis Geo 업데이트 실패하였지만 상점 생성은 완료 - storeId: {}, error: {}",
                    store.getId(), e.getMessage());
        }

        return store.getId();
    }

    public void updateStore(StoreUpdateRequest request,Long storeId, Long userId) {
        Store store = validateStoreExists(storeId);

        validateUserHasPermission(store, userId);

        store.update(request);

        // 위치 정보가 변경된 경우 Redis Geo 업데이트
        try {
            redisGeoService.updateStoreLocationFromEntity(store);
            log.info("상점 수정 완료 및 Redis Geo 업데이트 - storeId: {}", storeId);
        } catch (Exception e) {
            log.warn("Redis Geo 업데이트 실패하였지만 상점 수정은 완료 - storeId: {}, error: {}",
                    storeId, e.getMessage());
        }

        // 캐시 삭제
        cacheService.clearCache();
    }


    public void deleteStore(Long storeId, Long userId) {
        Store store = validateStoreExists(storeId);

        validateUserHasPermission(store, userId);
        store.markAsDeleted();

        // Redis Geo에서 상점 위치 정보 삭제
        try {
            redisGeoService.removeStoreLocation(storeId);
            log.info("상점 삭제 완료 및 Redis Geo에서 제거 - storeId: {}", storeId);
        } catch (Exception e) {
            log.warn("Redis Geo 제거 실패하였지만 상점 삭제는 완료 - storeId: {}, error: {}",
                    storeId, e.getMessage());
        }

        cacheService.clearCache();
    }


    private void validateUserHasPermission(Store store, Long userId) {
        if (store.getMemberId().equals(userId)) {
            throw new IllegalArgumentException();
        }
    }

    private Store validateStoreExists(Long storeId) {
        return storeRepository.findByStoreId(storeId)
                .orElseThrow( () -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }

}
