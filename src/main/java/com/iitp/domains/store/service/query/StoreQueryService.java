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
import com.iitp.global.common.response.CursorPaginationStoreResponse;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.redis.service.RedisGeoService;
import com.iitp.global.redis.service.StoreRedisService;
import com.iitp.imageUpload.service.query.ImageGetService;
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

    public StoreListTotalResponse findStores(
            Long memberId, Category category, String keyword, SortType sort, boolean direction, int limit,
            Long cursorId, Double cursorDistance, Double cursorReviewAvg, Long cursorReviewCnt
    ) {
        Location baseLocation = locationQueryService.getMemberBaseLocation(memberId);
        List<StoreListQueryResult> results = storeRepository.findStores(category, keyword, sort, direction, limit,
                cursorId, cursorDistance,cursorReviewAvg, cursorReviewCnt,
                 baseLocation.getLatitude(), baseLocation.getLongitude());
        List<StoreListResponse> stores = convertQueryResultToResponse(results);

        return new StoreListTotalResponse(
                stores.isEmpty() ? null : stores.getFirst().id(),
                stores.isEmpty() ? null : stores.getLast().id(),
                stores
        );
    }


    public StoreDetailResponse findStoreData(Long memberId, Long storeId) {
        // 캐시된 데이터가 있고, 다른 가게를 요청하는 경우
        if (cacheService.hasCachedData() && !storeId.equals(currentCachedStoreId)) {
            cacheService.clearCache();
        }

        // 캐시에서 데이터 조회
        StoreDetailResponse cachedData = cacheService.getCachedStoreDetail();
        if (cachedData != null && storeId.equals(currentCachedStoreId)) {
            return cachedData;
        }

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
        cacheService.cacheStoreDetail(response);
        currentCachedStoreId = storeId;
        return response;
    }

    public CursorPaginationStoreResponse<StoreListResponse> findFavoriteStores(
            long memberId,
            SortType sort,
            int limit,
            long cursorId,
            Double cursorDistance,
            Double cursorReviewAvg,
            Long cursorReviewCnt
    ) {
        Location baseLocation = locationQueryService.getMemberBaseLocation(memberId);
        List<StoreListQueryResult> result = storeRepository
                .findFavoriteStores(memberId, sort, limit,
                        cursorId, cursorDistance, cursorReviewAvg, cursorReviewCnt,
                        baseLocation.getLatitude(), baseLocation.getLongitude());
        List<StoreListResponse> stores = convertQueryResultToResponse(result);

        return new CursorPaginationStoreResponse<>(
                stores.isEmpty() ? null : stores.getFirst().id(),
                stores.isEmpty() ? null : stores.getLast().id(),
                stores.isEmpty() ? null : stores.getFirst().distance(),
                stores.isEmpty() ? null : stores.getLast().distance(),
                stores);
    }

    public Store findExistingStore(Long storeId) {
        Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
        if (store.getIsDeleted()) {
            throw new NotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }
        return store;
    }

    private String getImageUrl(String imageKey) {
        return imageGetService.getGetS3Url(imageKey).preSignedUrl();
    }

    private List<StoreListResponse> convertQueryResultToResponse(
            List<StoreListQueryResult> results
    ) {

        return results.stream()
                .map(result -> StoreListResponse.fromQueryResult(
                        result,
                        imageGetService.getGetS3Url(result.imageKey()).preSignedUrl(),
                        ((result.openTime().isBefore(LocalTime.now())) && (LocalTime.now()
                                .isBefore(result.closeTime())))
                                ? result.status()
                                : StoreStatus.CLOSED)
                ).toList();
    }
}
