package com.iitp.domains.member.service;

import com.iitp.domains.cart.dto.CartRedisDto;
import com.iitp.domains.cart.repository.CartRepository;
import com.iitp.domains.map.repository.MapRepository;
import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.dto.responseDto.DiscountMenuResponseDto;
import com.iitp.domains.member.dto.responseDto.LocationResponseDto;
import com.iitp.domains.member.dto.responseDto.MainOverviewResponseDto;
import com.iitp.domains.member.dto.responseDto.PopularStoreResponseDto;
import com.iitp.domains.member.repository.LocationRepository;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.domains.review.service.query.ReviewQueryService;
import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.redis.service.CartRedisService;
import com.iitp.global.redis.service.RedisGeoService;
import com.iitp.global.util.map.DistanceCalculator;
import com.iitp.imageUpload.service.query.ImageGetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iitp.global.common.constants.BusinessLogicConstants;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MainOverviewService {
    private final MemberQueryService memberQueryService;
    private final LocationRepository locationRepository;
//    private final NotificationRepository notificationRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final RedisGeoService redisGeoService;
    private final ReviewQueryService reviewQueryService;
    private final MapRepository mapRepository;
    private final DistanceCalculator distanceCalculator;
    private final ImageGetService imageGetService;
    private final CartRedisService cartRedisService;


    /**
     * 메인 페이지 전체 데이터 조회
     */
    public MainOverviewResponseDto getMainOverview(Long memberId) {
        log.debug("메인 페이지 데이터 조회 시작 - memberId: {}", memberId);

        Member member = memberQueryService.findMemberById(memberId);

        Integer cartCount = getCartCount(memberId);
//        Boolean notifications = hasNotifications(memberId); // 알람 구현시 추가
        LocationResponseDto location = getDefaultLocation(memberId);
        List<DiscountMenuResponseDto> discountMenus = getDiscountMenus(memberId);
        List<PopularStoreResponseDto> popularStores = getPopularStores(memberId);

        log.debug("메인 페이지 데이터 조회 완료 - memberId: {}, cartCount: {}, notifications: {}",
                memberId, cartCount);

        return MainOverviewResponseDto.of(
                cartCount,
//                notifications,
                location,
                discountMenus,
                popularStores
        );
    }

    /**
     * 장바구니 개수 조회
     */
    private Integer getCartCount(Long memberId) {
        try {
            String cacheKey = "cart:" + memberId;  // CART_CACHE_PREFIX + memberId
            CartRedisDto cart = cartRedisService.getCartFromRedis(cacheKey);

            if (cart == null || cart.menus() == null) {
                return 0;
            }

            // 장바구니에 있는 메뉴들의 총 수량 계산
            return cart.menus().stream()
                    .mapToInt(menu -> menu.orderQuantity())
                    .sum();

        } catch (Exception e) {
            log.error("장바구니 개수 조회 중 오류 발생 - memberId: {}", memberId, e);
            return 0;
        }
    }

    /**
     * 기본 주소 조회
     */
    private LocationResponseDto getDefaultLocation(Long memberId) {
        Location location = locationRepository.findByMemberIdAndIsMostRecentTrueAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new RuntimeException("기본 주소를 찾을 수 없습니다"));

        return LocationResponseDto.from(location);
    }

    /**
     * 할인 메뉴 목록 조회
     */
    private List<DiscountMenuResponseDto> getDiscountMenus(Long memberId) {
        log.debug("할인 메뉴 조회 시작 - memberId: {}", memberId);

        try {
            Location location = locationRepository.findByMemberIdAndIsMostRecentTrueAndIsDeletedFalse(memberId)
                    .orElse(null);

            if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
                log.warn("사용자 위치 정보가 없어 할인 메뉴를 조회할 수 없습니다 - memberId: {}", memberId);
                return List.of();
            }

            List<String> nearbyStoreIds = redisGeoService.findNearbyStores(
                    location.getLatitude(),
                    location.getLongitude(),
                    (double) BusinessLogicConstants.MAP_SEARCHING_RANGE_KM // 5km
            );

            if (nearbyStoreIds.isEmpty()) {
                log.debug("근처에 가게가 없습니다 - memberId: {}", memberId);
                return List.of();
            }

            List<Long> storeIds = nearbyStoreIds.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            List<Menu> discountMenus = menuRepository.findDiscountMenusByStoreIds(storeIds, 10);

            if (discountMenus.isEmpty()) {
                log.debug("근처에 할인 메뉴가 없습니다 - memberId: {}", memberId);
                return List.of();
            }

            Map<Long, Double> storeRatings = discountMenus.stream()
                    .map(menu -> menu.getStore().getId())
                    .distinct()
                    .collect(Collectors.toMap(
                            storeId -> storeId,
                            storeId -> reviewQueryService.calculateStoreRating(storeId)
                    ));

            return discountMenus.stream()
                    .map(menu -> {
                        Double rating = storeRatings.get(menu.getStore().getId());
                        // 메뉴 이미지 URL 생성
                        String imageUrl = getMenuImageUrl(menu);
                        return DiscountMenuResponseDto.from(menu, rating, imageUrl);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("할인 메뉴 조회 중 오류 발생 - memberId: {}", memberId, e);
            return List.of();
        }
    }

    /**
     * 인기 가게 목록 조회
     */
    private List<PopularStoreResponseDto> getPopularStores(Long memberId) {
        log.debug("인기 가게 조회 시작 - memberId: {}", memberId);

        try {
            Location location = locationRepository.findByMemberIdAndIsMostRecentTrueAndIsDeletedFalse(memberId)
                    .orElse(null);

            if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
                log.warn("사용자 위치 정보가 없어 인기 가게를 조회할 수 없습니다 - memberId: {}", memberId);
                return List.of();
            }

            List<String> nearbyStoreIds = redisGeoService.findNearbyStores(
                    location.getLatitude(),
                    location.getLongitude(),
                    (double) BusinessLogicConstants.MAP_SEARCHING_RANGE_KM //5km
            );

            if (nearbyStoreIds.isEmpty()) {
                log.debug("근처에 가게가 없습니다 - memberId: {}", memberId);
                return List.of();
            }

            List<Long> storeIds = nearbyStoreIds.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            List<Store> stores = mapRepository.findStoreListByIds(storeIds);

            if (stores.isEmpty()) {
                log.debug("조회된 가게가 없습니다 - memberId: {}", memberId);
                return List.of();
            }

            return stores.stream()
                    .map(store -> {
                        // 평점 계산
                        Double rating = reviewQueryService.calculateStoreRating(store.getId());

                        // 거리 계산
                        Double distance = distanceCalculator.calculateDistance(
                                location.getLatitude(), location.getLongitude(),
                                store.getLatitude(), store.getLongitude()
                        );

                        // 이미지 URL 가져오기
                        String imageUrl = getStoreImageUrl(store);

                        return PopularStoreResponseDto.from(store, imageUrl, distance, rating);
                    })
                    .sorted((a, b) -> Double.compare(b.ratingAvg(), a.ratingAvg())) // 평점 높은 순
                    .limit(10) // 최대 10개
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("인기 가게 조회 중 오류 발생 - memberId: {}", memberId, e);
            return List.of();
        }
    }

    private String getStoreImageUrl(Store store) {
        if (store.getStoreImages() != null && !store.getStoreImages().isEmpty()) {
            String imageKey = store.getStoreImages().get(0).getImageKey();
            return imageGetService.getGetS3Url(imageKey).preSignedUrl();
        }
        return null;
    }

    private String getMenuImageUrl(Menu menu) {
        if (menu.getImageKey() != null && !menu.getImageKey().isEmpty()) {
            return imageGetService.getGetS3Url(menu.getImageKey()).preSignedUrl();
        }
        return null; // 기본 이미지 또는 null
    }
}
