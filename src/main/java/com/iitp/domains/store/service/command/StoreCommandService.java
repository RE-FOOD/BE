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


    public void createStore(StoreCreateRequest request, Long userId) {
        // 주소로 위/경도 조회
        GeocodingResult geocodingResult = kakaoGeocodingService.getCoordinates(request.address());

        Store store = request.toEntity(userId, geocodingResult.latitude(), geocodingResult.longitude());

        storeRepository.save(store);

        for(String img : request.imageKey()){
             storeImageRepository.save(new StoreImage(img, store));
        }
    }

    public void updateStore(StoreUpdateRequest request,Long storeId, Long userId) {
        Store store = validateStoreExists(storeId);

        validateUserHasPermission(store, userId);

        store.update(request);

        // 캐시 삭제
        cacheService.clearCache();
    }


    public void deleteStore(Long storeId, Long userId) {
        Store store = validateStoreExists(storeId);

        validateUserHasPermission(store, userId);
        store.markAsDeleted();

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
