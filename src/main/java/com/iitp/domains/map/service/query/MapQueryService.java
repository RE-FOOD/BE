package com.iitp.domains.map.service.query;

import com.iitp.domains.map.dto.responseDto.MapListResponseDto;
import com.iitp.domains.map.dto.responseDto.MapListScrollResponseDto;
import com.iitp.domains.map.dto.responseDto.MapMarkerResponseDto;
import com.iitp.domains.map.dto.responseDto.MapSummaryResponseDto;
import com.iitp.domains.map.repository.MapRepository;
import com.iitp.domains.review.dto.response.ReviewResponse;
import com.iitp.domains.review.service.query.ReviewQueryService;
import com.iitp.domains.store.domain.SortType;
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
    private final ReviewQueryService reviewQueryService;

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
        List<Store> stores = mapRepository.findStoreListByIds(storeIds);
        ;

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

        // 리뷰 조회
        List<ReviewResponse> reviews = reviewQueryService.getStoreReviews(
                storeId, 0L, Integer.MAX_VALUE);

        Double rating = 0.0;
        Integer reviewCount = 0;

        if (!reviews.isEmpty()) {
            rating = reviews.stream()
                    .mapToInt(ReviewResponse::rating)
                    .average()
                    .orElse(0.0);
            rating = Math.round(rating * 10.0) / 10.0;
            reviewCount = reviews.size();
        }

        return MapSummaryResponseDto.from(store, imageUrl, pickupTime, distance,
                rating, reviewCount);
    }

    /**
     * 근처 가게 목록 조회
     */
    public List<MapListResponseDto> getNearbyStoreList(Double latitude, Double longitude, Double radiusKm,
                                                       SortType sort) {
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

                    // 리뷰 조회
                    List<ReviewResponse> reviews = reviewQueryService.getStoreReviews(
                            store.getId(), 0L, Integer.MAX_VALUE);

                    Double rating = 0.0;
                    Integer reviewCount = 0;

                    if (!reviews.isEmpty()) {
                        rating = reviews.stream()
                                .mapToInt(ReviewResponse::rating)
                                .average()
                                .orElse(0.0);
                        rating = Math.round(rating * 10.0) / 10.0;
                        reviewCount = reviews.size();
                    }

                    return MapListResponseDto.from(store, imageUrl, distance, rating, reviewCount);
                })
                .collect(Collectors.toList());

        // 정렬 적용
        return applySorting(storeList, sort);
    }

    /**
     * 근처 가게 목록 조회 - 무한스크롤
     */
    public MapListScrollResponseDto getNearbyStoreListWithScroll(Double latitude, Double longitude,
                                                                 Double radiusKm, SortType sort,
                                                                 Long cursorId, Integer limit) {
        log.info("근처 가게 목록 무한스크롤 조회 - lat: {}, lng: {}, radius: {}km, sort: {}, cursor: {}, limit: {}",
                latitude, longitude, radiusKm, sort, cursorId, limit);

        List<String> nearbyStoreIds = redisGeoService.findNearbyStores(latitude, longitude, radiusKm);

        if (nearbyStoreIds.isEmpty()) {
            return MapListScrollResponseDto.empty(cursorId);
        }

        List<Long> storeIds = nearbyStoreIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // 전체 가게 정보 조회
        List<Store> stores = mapRepository.findStoreListByIds(storeIds);

        if (stores.isEmpty()) {
            return MapListScrollResponseDto.empty(cursorId);
        }

        List<MapListResponseDto> allStoreList = stores.stream()
                .map(store -> {
                    String imageUrl = getStoreImageUrl(store);
                    Double distance = distanceCalculator.calculateDistance(
                            latitude, longitude,
                            store.getLatitude(), store.getLongitude());

                    List<ReviewResponse> reviews = reviewQueryService.getStoreReviews(
                            store.getId(), 0L, Integer.MAX_VALUE);

                    Double rating = 0.0;
                    Integer reviewCount = 0;

                    if (!reviews.isEmpty()) {
                        rating = reviews.stream()
                                .mapToInt(ReviewResponse::rating)
                                .average()
                                .orElse(0.0);
                        rating = Math.round(rating * 10.0) / 10.0;
                        reviewCount = reviews.size();
                    }

                    return MapListResponseDto.from(store, imageUrl, distance, rating, reviewCount);
                })
                .collect(Collectors.toList());

        // 정렬 적용 (항상 적용)
        List<MapListResponseDto> sortedStoreList = applySorting(allStoreList, sort);

        // 애플리케이션 레벨에서 커서 기반 페이징 적용
        MapListScrollResponseDto result = applyCursorPagination(sortedStoreList, cursorId, limit);

        // cursorId 필드에 요청한 cursorId 설정하여 반환
        return MapListScrollResponseDto.of(
                result.stores(),
                cursorId,
                result.prevCursor(),
                result.nextCursor(),
                result.hasNext()
        );
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
    private List<MapListResponseDto> applySorting(List<MapListResponseDto> storeList, SortType sort) {
        if (sort == null) {
            sort = SortType.NEAR; // 기본값 설정
        }

        return switch (sort) {
            case NEAR -> storeList.stream()
                    .sorted(Comparator.comparing(MapListResponseDto::distance))
                    .collect(Collectors.toList());
            case REVIEW -> storeList.stream()
                    .sorted(Comparator.comparing(MapListResponseDto::reviewCount, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
            case RATING -> storeList.stream()
                    .sorted(Comparator.comparing(MapListResponseDto::rating, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
            default -> storeList.stream()
                    .sorted(Comparator.comparing(MapListResponseDto::distance))
                    .collect(Collectors.toList());
        };
    }

    /**
     * 커서 기반 페이징 적용
     */
private MapListScrollResponseDto applyCursorPagination(List<MapListResponseDto> storeList,
                                                       Long cursorId, Integer limit) {
    if (storeList.isEmpty()) {
        return MapListScrollResponseDto.builder()
                .stores(List.of())
                .cursorId(null)
                .prevCursor(null)
                .nextCursor(null)
                .hasNext(false)
                .build();
    }

    int startIndex = 0;

    // cursorId가 있는 경우 해당 위치 찾기
    if (cursorId != null) {
        for (int i = 0; i < storeList.size(); i++) {
            if (storeList.get(i).id().equals(cursorId)) {
                startIndex = i + 1; // 커서 다음부터 시작
                break;
            }
        }
    }

    // 시작 인덱스가 리스트 크기를 넘으면 빈 결과 반환
    if (startIndex >= storeList.size()) {
        return MapListScrollResponseDto.builder()
                .stores(List.of())
                .cursorId(null)
                .prevCursor(null)
                .nextCursor(null)
                .hasNext(false)
                .build();
    }

    // limit만큼 데이터 가져오기
    int endIndex = Math.min(startIndex + limit, storeList.size());
    List<MapListResponseDto> pagedList = storeList.subList(startIndex, endIndex);

    // 커서 정보 계산
    Long prevCursor = (startIndex > 0) ? storeList.get(startIndex - 1).id() : null;
    Long nextCursor = (endIndex < storeList.size()) ? pagedList.get(pagedList.size() - 1).id() : null;
    Boolean hasNext = endIndex < storeList.size();

    return MapListScrollResponseDto.builder()
            .stores(pagedList)
            .cursorId(null) // 여기서는 null로 설정, 상위에서 설정
            .prevCursor(prevCursor)
            .nextCursor(nextCursor)
            .hasNext(hasNext)
            .build();
}
}