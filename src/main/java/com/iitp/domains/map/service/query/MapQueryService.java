package com.iitp.domains.map.service.query;

import com.iitp.domains.map.dto.responseDto.MapListResponseDto;
import com.iitp.domains.map.dto.responseDto.MapMarkerResponseDto;
import com.iitp.domains.map.dto.responseDto.MapSummaryResponseDto;
import com.iitp.domains.map.repository.MapRepository;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.redis.service.RedisGeoService;
import com.iitp.global.util.map.DistanceCalculator;
import com.iitp.imageUpload.service.query.ImageGetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MapQueryService {
    private final MapRepository mapRepository;
    private final RedisGeoService redisGeoService;
    private final ImageGetService imageGetService;
    private final DistanceCalculator distanceCalculator;

    /**
     * 근처 가게 마커 조회
     */
    public List<MapMarkerResponseDto> getNearbyStoreMarkers(Double latitude, Double longitude, Double radiusKm) {
        log.info("근처 가게 마커 조회 - lat: {}, lng: {}, radius: {}km", latitude, longitude, radiusKm);

        // Redis GEO에서 근처 가게 ID 조회
        List<String> nearbyStoreIds = redisGeoService.findNearbyStores(latitude, longitude, radiusKm);

        if (nearbyStoreIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 가게 ID 리스트를 Long으로 변환
        List<Long> storeIds = nearbyStoreIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // DB에서 가게 정보 조회
        List<Store> stores = mapRepository.findStoreListByIds(storeIds);;

        return stores.stream()
                .map(MapMarkerResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 가게 지도 요약 조회 (지도 하단)
     */
    public MapSummaryResponseDto getStoreSummary(Long storeId, Double userLatitude, Double userLongitude) {
        log.info("가게 요약 정보 조회 - storeId: {}", storeId);

        // 가게 정보 조회
        Store store = mapRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));

        // 이미지 URL 조회
        String imageUrl = getStoreImageUrl(store);

        // 거리 계산
        Double distance = distanceCalculator.calculateDistance(
                userLatitude, userLongitude,
                store.getLatitude(), store.getLongitude());

        // 픽업 가능 시간 생성
        String pickupTime = generatePickupTime(store);

        // TODO: 리뷰 정보 조회 (임시 데이터)
        Double rating = 3.5;
        Integer reviewCount = 1030;

        return MapSummaryResponseDto.from(store, imageUrl, pickupTime, distance, rating, reviewCount);
    }

    /**
     * 근처 가게 목록 조회
     */
    public List<MapListResponseDto> getNearbyStoreList(Double latitude, Double longitude, Double radiusKm, String sort) {
        log.info("근처 가게 목록 조회 - lat: {}, lng: {}, radius: {}km, sort: {}", latitude, longitude, radiusKm, sort);

        // Redis GEO에서 근처 가게 ID 조회
        List<String> nearbyStoreIds = redisGeoService.findNearbyStores(latitude, longitude, radiusKm);

        if (nearbyStoreIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 가게 ID 리스트를 Long으로 변환
        List<Long> storeIds = nearbyStoreIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // DB에서 가게 정보 조회
        List<Store> stores = mapRepository.findStoreListByIds(storeIds);

        // 응답 DTO 생성
        List<MapListResponseDto> storeList = stores.stream()
                .map(store -> {
                    String imageUrl = getStoreImageUrl(store);
                    Double distance = distanceCalculator.calculateDistance(
                            latitude, longitude,
                            store.getLatitude(), store.getLongitude());

                    // TODO: 리뷰 정보 조회 (임시 데이터)
                    Double rating = 3.5;
                    Integer reviewCount = 1030;

                    return MapListResponseDto.from(store, imageUrl, distance, rating, reviewCount);
                })
                .collect(Collectors.toList());

        // 정렬 적용
        return applySorting(storeList, sort);
    }

    /**
     * 가게 이미지 URL 조회
     */
    private String getStoreImageUrl(Store store) {
        if (store.getStoreImages() != null && !store.getStoreImages().isEmpty()) {
            String imageKey = store.getStoreImages().get(0).getImageKey();
            return imageGetService.getGetS3Url(imageKey).preSignedUrl();
        }
        return null;
    }

    /**
     * 픽업 가능 시간 생성
     */
    private String generatePickupTime(Store store) {
        LocalTime openTime = store.getOpenTime();
        LocalTime closeTime = store.getCloseTime();

        return String.format("픽업 : 오늘 %s-%s",
                openTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                closeTime.format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    /**
     * 정렬 적용
     */
    private List<MapListResponseDto> applySorting(List<MapListResponseDto> storeList, String sort) {
        return switch (sort) {
            case "거리순" -> storeList.stream()
                    .sorted(Comparator.comparing(MapListResponseDto::distance))
                    .collect(Collectors.toList());
            case "리뷰순" -> storeList.stream()
                    .sorted(Comparator.comparing(MapListResponseDto::reviewCount, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
            case "평점순" -> storeList.stream()
                    .sorted(Comparator.comparing(MapListResponseDto::rating, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
            default -> storeList.stream()
                    .sorted(Comparator.comparing(MapListResponseDto::distance))
                    .collect(Collectors.toList());
        };
    }
}