package com.iitp.domains.store.service.query;

import com.iitp.domains.favorite.service.query.FavoriteQueryService;
import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.service.query.LocationQueryService;
import com.iitp.domains.review.repository.ReviewRepository;
import com.iitp.domains.review.repository.mapper.ReviewAggregationResult;
import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.SortType;
import com.iitp.domains.store.domain.StoreStatus;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.dto.response.MenuListResponse;
import com.iitp.domains.store.dto.response.StoreDetailResponse;
import com.iitp.domains.store.dto.response.StoreListResponse;
import com.iitp.domains.store.dto.response.StoreListTotalResponse;
import com.iitp.domains.store.repository.mapper.StoreListQueryResult;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.common.response.TwoWayCursorListResponse;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.redis.service.RedisGeoService;
import com.iitp.global.redis.service.StoreRedisService;
import com.iitp.imageUpload.service.query.ImageGetService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StoreQueryService {
    private final StoreRepository storeRepository;
    private final ImageGetService imageGetService;
    private final MenuQueryService menuQueryService;
    private final LocationQueryService locationQueryService;
    private final FavoriteQueryService favoriteQueryService;
    private final ReviewRepository reviewRepository;    // 의존성 리팩토링 고민
    private final StoreRedisService cacheService;
    private final RedisGeoService redisGeoService;
    private Long currentCachedStoreId = null;

    public StoreListTotalResponse findStores(Long memberId, Category category, String keyword, SortType sort,
                                             Long cursorId, boolean direction, int limit) {
        Location memberLocation = locationQueryService.getMemberBaseLocation(memberId);
        List<StoreListQueryResult> results = storeRepository
                .findStores(category, keyword, sort, cursorId, direction, limit);
        List<StoreListResponse> stores = convertQueryResultToResponse(results, memberLocation);

        return new StoreListTotalResponse(stores.getFirst().id(), stores.getLast().id(), stores);
    }


    public StoreDetailResponse findStoreData(Long memberId, Long storeId) {
        // 캐시된 데이터가 있고, 다른 가게를 요청하는 경우
//        if (cacheService.hasCachedData() && !storeId.equals(currentCachedStoreId)) {
//            cacheService.clearCache();
//        }
//
//        // 캐시에서 데이터 조회
//        StoreDetailResponse cachedData = cacheService.getCachedStoreDetail();
//        if (cachedData != null && storeId.equals(currentCachedStoreId)) {
//            return cachedData;
//        }

        Store store = findExistingStore(storeId);

        List<String> imageUrls = store.getStoreImages().stream()
                .map(result -> getImageUrl(result.getImageKey()))
                .collect(Collectors.toList());

        List<MenuListResponse> menus = menuQueryService.findMenus(storeId);

        // 리팩토링 고민: 쿼리 최적화. 아예 store repository에서 Projection을 통해 review 집계 정보, favorite 여부 가져오기
        boolean isFavored = favoriteQueryService.isFavoriteExists(memberId, storeId);

        Optional<ReviewAggregationResult> reviewAggregation = reviewRepository.findReviewRatingAverageByStore(storeId);
        Double roundedAvg = reviewAggregation.map(ReviewAggregationResult::averageRating).orElse(null);
        long reviewCount = reviewAggregation.map(ReviewAggregationResult::count).orElse(0L);

        StoreDetailResponse response = StoreDetailResponse
                .from(store, imageUrls, menus, isFavored, roundedAvg, reviewCount);

        // 캐시에 저장
//        cacheService.cacheStoreDetail(response);
//        currentCachedStoreId = storeId;
        return response;
    }

    public TwoWayCursorListResponse<StoreListResponse> findFavoriteStores(
            long memberId,
            SortType sort,
            long cursorId,
            int limit
    ) {
        List<StoreListQueryResult> result = storeRepository.findFavoriteStores(memberId, sort, cursorId, limit);
        Location baseLocation = locationQueryService.getMemberBaseLocation(memberId);
        List<StoreListResponse> stores = convertQueryResultToResponse(result, baseLocation);

        return new TwoWayCursorListResponse<>(stores.getFirst().id(), stores.getLast().id(), stores);
    }

    public Store findExistingStore(Long storeId) {
        Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
        System.out.println("store.getId() = " + store.getId());
        System.out.println("store.getName() = " + store.getName());
        System.out.println("store.getFavorites() = " + store.getFavorites());
        store.getFavorites().forEach(it-> System.out.println("it.getId() = " + it.getId()));


        if (store.getIsDeleted()) {
            throw new NotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }
        return store;
    }

    private String getImageUrl(String imageKey) {
        return imageGetService.getGetS3Url(imageKey).preSignedUrl();
    }

    private List<StoreListResponse> convertQueryResultToResponse(
            List<StoreListQueryResult> results,
            Location location
    ) {

        return results.stream()
                .map(result -> {
                    String imageUrl = imageGetService.getGetS3Url(result.imageKey()).preSignedUrl();
                    double distance = redisGeoService.getKiloMeterDistanceToStore(
                            result.id(),
                            location.getLatitude(),
                            location.getLongitude());

                    log.info(String.valueOf(result.openTime().isAfter(LocalTime.now())));

                    return StoreListResponse.fromQueryResult(
                            result,
                            imageUrl,
                            distance,
                            ((result.openTime().isBefore(LocalTime.now())) && (LocalTime.now()
                                    .isBefore(result.closeTime())))
                                    ? result.status()
                                    : StoreStatus.CLOSED
                    );
                }).toList();
    }
}
