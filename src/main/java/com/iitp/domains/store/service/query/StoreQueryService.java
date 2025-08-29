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
import com.iitp.domains.store.dto.response.*;
import com.iitp.domains.store.repository.mapper.StoreListQueryResult;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.common.response.TwoWayCursorListResponse;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.redis.service.RedisGeoService;
import com.iitp.global.redis.service.StoreRedisService;
import com.iitp.imageUpload.service.query.ImageGetService;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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
    private final ReviewRepository reviewRepository;
    private final StoreRedisService cacheService;
    private final RedisGeoService redisGeoService;
    private Long currentCachedStoreId = null;

    /**
     * 가게 목록 조회 (거리순은 Redis GEO, 나머지는 Cursor)
     */
    public StoreListTotalResponse findStores(Long memberId, Category category, String keyword, SortType sort,
                                             Long cursorId, boolean direction, int limit) {

        // 거리순 정렬은 Redis GEO 기반 처리
        if (sort == SortType.NEAR) {
            return findStoresByRedisGeo(memberId, category, keyword, cursorId, direction, limit);
        }

        // 나머지 정렬은 기존 Cursor 기반 페이징 사용
        return findStoresByCursor(memberId, category, keyword, sort, cursorId, direction, limit);
    }

    /**
     * 거리순 정렬 - Redis GEO 기반 페이징
     */
    private StoreListTotalResponse findStoresByRedisGeo(
            Long memberId,
            Category category,
            String keyword,
            Long cursorId,
            boolean direction,
            int limit
    ) {
        Location memberLocation = locationQueryService.getMemberBaseLocation(memberId);

        List<String> nearbyStoreIds = redisGeoService.findNearbyStores(
                memberLocation.getLatitude(),
                memberLocation.getLongitude(),
                5.0  // 5km 반경
        );

        if (nearbyStoreIds.isEmpty()) {
            return StoreListTotalResponse.empty();
        }

        // 커서 기반 페이징을 Redis 결과에 적용
        PaginatedStoreIds paginatedIds = applyPaginationToStoreIds(
                nearbyStoreIds, cursorId, direction, limit
        );

        List<StoreListQueryResult> results = storeRepository
                .findStoresByIds(paginatedIds.getStoreIds(), category, keyword);

        if (results.isEmpty()) {
            return StoreListTotalResponse.empty();
        }

        // Redis 순서대로 정렬
        List<StoreListQueryResult> sortedResults = sortByRedisOrder(results, paginatedIds.getStoreIds());

        List<StoreListResponse> stores = convertQueryResultToResponse(sortedResults, memberLocation);

        return StoreListTotalResponse.ofWithAccuratePagination(
                stores,
                paginatedIds.isHasPrev(),
                paginatedIds.isHasNext()
        );
    }

    /**
     * 기존 커서 기반 조회 (리뷰순, 평점순)
     */
    private StoreListTotalResponse findStoresByCursor(
            Long memberId,
            Category category,
            String keyword,
            SortType sort,
            Long cursorId,
            boolean direction,
            int limit
    ) {
        Location memberLocation = locationQueryService.getMemberBaseLocation(memberId);

        List<StoreListQueryResult> results = storeRepository
                .findStores(category, keyword, sort, cursorId, direction, limit);

        if (results.isEmpty()) {
            return StoreListTotalResponse.empty();
        }

        PaginationInfo paginationInfo = calculatePaginationInfo(results, cursorId, direction, limit);

        List<StoreListQueryResult> actualResults = paginationInfo.getActualResults();

        List<StoreListResponse> stores = convertQueryResultToResponse(actualResults, memberLocation);

        return StoreListTotalResponse.ofWithAccuratePagination(
                stores,
                paginationInfo.isHasPrev(),
                paginationInfo.isHasNext()
        );
    }

    /**
     * 가게 상세 정보 조회 (캐시 적용)
     */
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

    /**
     * 존재하는 가게 조회 (삭제된 가게 제외)
     */
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

    /**
     * 이미지 URL 생성
     */
    private String getImageUrl(String imageKey) {
        return imageGetService.getGetS3Url(imageKey).preSignedUrl();
    }

    /**
     * QueryResult를 Response로 변환 (거리 계산 및 영업상태 결정)
     */
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

                    // 영업시간 체크해서 실제 상태 결정
                    StoreStatus actualStatus = isStoreOpen(result.openTime(), result.closeTime())
                            ? result.status()
                            : StoreStatus.CLOSED;

                    return StoreListResponse.fromQueryResult(
                            result,
                            imageUrl,
                            distance,
                            actualStatus
                    );
                }).toList();
    }

    /**
     * 찜한 가게 목록 조회 (단방향 무한 스크롤)
     */
    public FavoriteStoresResponse findMyFavoriteStores(
            Long memberId,
            SortType sort,
            Long cursorId,
            int limit
    ) {
        // 사용자 위치 정보 조회
        Location userLocation = locationQueryService.getMemberBaseLocation(memberId);

        // 찜한 가게 목록 조회
        List<StoreListQueryResult> queryResults = storeRepository
                .findFavoriteStoresForward(memberId, sort, cursorId != null ? cursorId : 0L, limit + 1);

        if (queryResults.isEmpty()) {
            return FavoriteStoresResponse.empty();
        }

        boolean hasNext = queryResults.size() > limit;

        List<StoreListQueryResult> actualResults = hasNext
                ? queryResults.subList(0, limit)
                : queryResults;

        List<FavoriteStoreItem> favoriteStores = actualResults.stream()
                .map(result -> convertToFavoriteStoreItem(result, userLocation))
                .toList();

        return FavoriteStoresResponse.of(favoriteStores, cursorId, hasNext);
    }

    private FavoriteStoreItem convertToFavoriteStoreItem(
            StoreListQueryResult result,
            Location userLocation
    ) {
        // 이미지 URL 생성
        String imageUrl = imageGetService.getGetS3Url(result.imageKey()).preSignedUrl();

        // 거리 계산
        double distance = redisGeoService.getKiloMeterDistanceToStore(
                result.id(),
                userLocation.getLatitude(),
                userLocation.getLongitude());

        // 실제 영업 상태 결정
        StoreStatus actualStatus = isStoreOpen(result.openTime(), result.closeTime())
                ? StoreStatus.OPEN
                : StoreStatus.CLOSED;

        return FavoriteStoreItem.fromQueryResult(result, imageUrl, distance, actualStatus);
    }

    /**
     * Redis 결과에 커서 기반 페이징 적용
     */
    private PaginatedStoreIds applyPaginationToStoreIds(
            List<String> nearbyStoreIds,
            Long cursorId,
            boolean direction,
            int limit
    ) {
        List<Long> storeIds = nearbyStoreIds.stream()
                .map(Long::valueOf)
                .toList();

        int cursorIndex = -1;
        if (cursorId != null && cursorId > 0) {
            cursorIndex = storeIds.indexOf(cursorId);
        }

        List<Long> pagedStoreIds;
        boolean hasPrev;
        boolean hasNext;

        if (direction) {
            // 다음 페이지
            int startIndex = cursorIndex + 1;
            hasPrev = cursorIndex >= 0;

            if (startIndex >= storeIds.size()) {
                // 더 이상 데이터 없음
                return new PaginatedStoreIds(List.of(), hasPrev, false);
            }

            int endIndex = Math.min(startIndex + limit + 1, storeIds.size());
            List<Long> candidates = storeIds.subList(startIndex, endIndex);

            hasNext = candidates.size() > limit;
            pagedStoreIds = hasNext ? candidates.subList(0, limit) : candidates;

        } else {
            // 이전 페이지
            hasNext = cursorIndex >= 0 && cursorIndex < storeIds.size();

            int endIndex = cursorIndex > 0 ? cursorIndex : storeIds.size();
            int startIndex = Math.max(0, endIndex - limit - 1);

            List<Long> candidates = storeIds.subList(startIndex, endIndex);
            hasPrev = startIndex > 0;

            pagedStoreIds = candidates.size() > limit ? candidates.subList(1, candidates.size()) : candidates;
        }

        return new PaginatedStoreIds(pagedStoreIds, hasPrev, hasNext);
    }

    /**
     * DB 조회 결과를 Redis 순서대로 정렬
     */
    private List<StoreListQueryResult> sortByRedisOrder(
            List<StoreListQueryResult> results,
            List<Long> orderedStoreIds
    ) {
        // ID를 키로 하는 Map 생성
        Map<Long, StoreListQueryResult> resultMap = results.stream()
                .collect(Collectors.toMap(StoreListQueryResult::id, Function.identity()));

        // Redis 순서대로 재정렬
        return orderedStoreIds.stream()
                .map(resultMap::get)
                .filter(Objects::nonNull)  // DB에서 찾지 못한 경우 제외 (삭제된 데이터 등)
                .toList();
    }

    private PaginationInfo calculatePaginationInfo(
            List<StoreListQueryResult> results,
            Long cursorId,
            boolean direction,
            int limit
    ) {
        boolean hasPrev = false;
        boolean hasNext = false;
        List<StoreListQueryResult> actualResults;

        if (direction) {
            // 다음 페이지 요청
            hasPrev = cursorId != null && cursorId > 0;
            hasNext = results.size() > limit;

            // 실제 반환할 데이터 (limit개만)
            actualResults = hasNext ? results.subList(0, limit) : results;

        } else {
            // 이전 페이지 요청 - 복잡한 케이스
            // direction=false는 두 번의 쿼리가 합쳐질 수 있어서 복잡함

            if (results.size() <= limit) {
                // 단일 쿼리 결과 (before 쿼리만으로 충분)
                hasPrev = results.size() > limit;
                hasNext = true; // 이전 페이지를 요청했다는 것은 다음이 있다는 의미
                actualResults = hasPrev ? results.subList(0, limit) : results;
            } else {
                // 복합 쿼리 결과 (before + after 합쳐진 경우)
                hasPrev = true; // 복합 쿼리가 실행됐다는 것은 이전이 있다는 의미
                hasNext = results.size() > limit;
                actualResults = results.size() > limit ? results.subList(0, limit) : results;
            }
        }

        return new PaginationInfo(actualResults, hasPrev, hasNext);
    }

    /**
     * 영업시간 체크 헬퍼 메서드
     */
    private boolean isStoreOpen(LocalTime openTime, LocalTime closeTime) {
        LocalTime now = LocalTime.now();
        return openTime.isBefore(now) && now.isBefore(closeTime);
    }

    /**
     * Redis 페이징 결과를 담는 내부 클래스
     */
    private static class PaginatedStoreIds {
        private final List<Long> storeIds;
        private final boolean hasPrev;
        private final boolean hasNext;

        public PaginatedStoreIds(List<Long> storeIds, boolean hasPrev, boolean hasNext) {
            this.storeIds = storeIds;
            this.hasPrev = hasPrev;
            this.hasNext = hasNext;
        }

        public List<Long> getStoreIds() { return storeIds; }
        public boolean isHasPrev() { return hasPrev; }
        public boolean isHasNext() { return hasNext; }
    }

    /**
     * 페이지네이션 정보 내부 클래스
     */
    private static class PaginationInfo {
        private final List<StoreListQueryResult> actualResults;
        private final boolean hasPrev;
        private final boolean hasNext;

        public PaginationInfo(List<StoreListQueryResult> actualResults, boolean hasPrev, boolean hasNext) {
            this.actualResults = actualResults;
            this.hasPrev = hasPrev;
            this.hasNext = hasNext;
        }

        public List<StoreListQueryResult> getActualResults() {
            return actualResults;
        }

        public boolean isHasPrev() {
            return hasPrev;
        }

        public boolean isHasNext() {
            return hasNext;
        }
    }
}