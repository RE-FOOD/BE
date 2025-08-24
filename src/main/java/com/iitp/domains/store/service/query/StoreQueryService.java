package com.iitp.domains.store.service.query;

import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.service.query.LocationQueryService;
import com.iitp.domains.member.service.query.MemberQueryService;
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
import java.time.LocalTime;
import java.util.List;
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
    private final MemberQueryService memberQueryService;
    private final LocationQueryService locationQueryService;
    private final StoreRedisService cacheService;
    private final RedisGeoService redisGeoService;
    private Long currentCachedStoreId = null;

    public StoreListTotalResponse findStores(Long memberId, Category category, String keyword, SortType sort,
                                             Long cursorId, boolean direction, int limit) {
        Location memberLocation = locationQueryService.getMemberBaseLocation(memberId);
        List<StoreListQueryResult> results = storeRepository.findStores(category, keyword, sort, cursorId, direction, limit);
        List<StoreListResponse> stores = convertQueryResultToResponse(results, memberLocation);

        return new StoreListTotalResponse(stores.getFirst().id(), stores.getLast().id(), stores);
    }


    public StoreDetailResponse findStoreData(Long storeId) {
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

        // DB에서 데이터 조회
        Store store = findExistingStore(storeId);

        List<String> imageUrls = store.getStoreImages().stream()
                .map(result -> getImageUrl(result.getImageKey()))
                .collect(Collectors.toList());

        List<MenuListResponse> menus = menuQueryService.findMenus(storeId);

        // TODO :: 리뷰 연동되면 수정
        boolean like = true;
        double ratingAvg = 3.5;
        int count = 100;

        StoreDetailResponse response = StoreDetailResponse.from(store, imageUrls, menus, like, ratingAvg, count);

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
