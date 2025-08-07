package com.iitp.domains.store.service.command;

import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.domain.entity.StoreImage;
import com.iitp.domains.store.dto.request.StoreCreateRequest;
import com.iitp.domains.store.repository.store.StoreImageRepository;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.geoCode.GeocodingResult;
import com.iitp.global.geoCode.KakaoGeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreCommandService {
    private final KakaoGeocodingService  kakaoGeocodingService;
    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;

    public void createStore(StoreCreateRequest request, Long userId) {
        // 주소로 위/경도 조회
        GeocodingResult geocodingResult = kakaoGeocodingService.getCoordinates(request.address());

        Store store = request.toEntity(userId, geocodingResult.latitude(), geocodingResult.longitude());

        storeRepository.save(store);

        for(String img : request.imageKey()){
             storeImageRepository.save(new StoreImage(img, store));
        }
    }
}
