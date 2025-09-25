package com.iitp.domains.store.service.query;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.service.query.CartQueryService;
import com.iitp.domains.favorite.service.query.FavoriteQueryService;
import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.service.query.LocationQueryService;
import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.repository.OrderRepository;
import com.iitp.domains.order.service.query.OrderQueryService;
import com.iitp.domains.review.repository.ReviewRepository;
import com.iitp.domains.review.repository.mapper.ReviewAggregationResult;
import com.iitp.domains.review.service.query.ReviewQueryService;
import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.SortType;
import com.iitp.domains.store.domain.StoreStatus;
import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.dto.response.*;
import com.iitp.domains.store.repository.mapper.StoreListQueryResult;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.domains.store.validator.MenuValidator;
import com.iitp.domains.store.validator.StoreValidator;
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


public interface StoreQueryService {

    /**
     * 가게 목록 조회 (거리순은 Redis GEO, 나머지는 Cursor)
     */
    StoreListTotalResponse findStores(Long memberId, Category category, String keyword, SortType sort,
                                             Long cursorId, boolean direction, int limit);


    /**
     * 가게 상세 정보 조회 (캐시 적용)
     */
    StoreDetailResponse findStoreData(Long memberId, Long storeId);


    Store findExistingMemer(Long memberId);


    /**
     * 찜한 가게 목록 조회 (단방향 무한 스크롤)
     */
    FavoriteStoresResponse findMyFavoriteStores(
            Long memberId,
            SortType sort,
            Long cursorId,
            int limit
    );


    List<StoreOrderListResponse> findOrders(Long memberId, Long cursorId);

    // 가게 메뉴 관리 페이지 데이터 반화
    List<StoreMenuManageResponse> findStoreMenuManage(Long cursorId, Long memberId);

}