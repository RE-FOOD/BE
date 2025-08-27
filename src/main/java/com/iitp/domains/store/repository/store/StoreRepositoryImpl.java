package com.iitp.domains.store.repository.store;

import static com.iitp.domains.favorite.domain.entity.QFavorite.favorite;
import static com.iitp.domains.review.domain.entity.QReview.review;
import static com.iitp.domains.store.domain.entity.QStore.store;
import static com.iitp.domains.store.domain.entity.QStoreImage.storeImage;

import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.SortType;
import com.iitp.domains.store.domain.entity.QMenu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.mapper.StoreListQueryResult;
import com.iitp.global.util.query.QueryExpressionFormatter;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {
    private final JPAQueryFactory queryFactory;


    /**
     * 가게 목록 조회 (커서 기반 페이징: 리뷰순, 평점순용)
     */
    @Override
    public List<StoreListQueryResult> findStores(Category category, String keyword, SortType sort,
                                                 Long cursorId, boolean direction, int limit) {

        QMenu menu = QMenu.menu;  // 메뉴 엔티티 추가

        JPAQuery<StoreListQueryResult> baseQuery = queryFactory
                .selectDistinct(Projections.constructor(StoreListQueryResult.class,
                        store.id,
                        store.name,
                        store.status,
                        QueryExpressionFormatter.getImageKeyPath(storeImage.imageKey),
                        store.maxPercent,
                        QueryExpressionFormatter.roundDoubleByFirstDecimalPlace(review.rating.avg()),
                        review.countDistinct(),
                        store.openTime,
                        store.closeTime))
                .from(store)
                .leftJoin(store.storeImages, storeImage)
                .leftJoin(store.reviews, review)
                .leftJoin(store.menus, menu)  // 메뉴 조인 추가
                .where(
                        store.isDeleted.eq(false),
                        eqCategory(category),
                        eqKeyword(keyword))  // 수정된 메서드 사용
                .groupBy(store.id, store.name, store.address, store.category);

        // direction = true: 다음 페이지 (cursorId 이후 데이터 조회)
        if (direction) {
            return baseQuery
                    .where(getCursorCondition(cursorId, sort, true))
                    .orderBy(getOrderSpecifiers(sort, true))
                    .limit(limit + 1)
                    .fetch();
        }
        // direction = false: 이전 페이지 (복잡한 로직)
        else {
            // 1단계: cursorId 이전의 값들을 먼저 조회
            List<StoreListQueryResult> beforeResults = baseQuery
                    .where(getCursorCondition(cursorId, sort, false))
                    .orderBy(getOrderSpecifiers(sort, false))
                    .limit(limit + 1)  // 하나 더 조회해서 hasPrev 판단
                    .fetch();

            // 2단계: cursorId 이전 값들이 limit에 못 미치면, cursorId 이후 값들도 조회
            if (beforeResults.size() <= limit) {
                int remainingLimit = limit - Math.min(beforeResults.size(), limit);

                if (remainingLimit > 0) {
                    List<StoreListQueryResult> afterResults = baseQuery
                            .where(getCursorCondition(cursorId, sort, true))
                            .orderBy(getOrderSpecifiers(sort, true))
                            .limit(remainingLimit + 1)  // 하나 더 조회해서 hasNext 판단
                            .fetch();

                    // 3단계: 두 결과를 합치고 정렬 기준에 맞게 정렬
                    List<StoreListQueryResult> combinedResults = new ArrayList<>();
                    combinedResults.addAll(beforeResults);
                    combinedResults.addAll(afterResults);

                    return applySorting(combinedResults, sort);
                }
            }

            // cursorId 이전의 값들만으로 limit을 채운 경우
            return applySorting(beforeResults, sort);
        }
    }


    /**
     * ID 목록으로 가게 조회
     */
    @Override
    public List<StoreListQueryResult> findStoresByIds(
            List<Long> storeIds,
            Category category,
            String keyword
    ) {
        if (storeIds.isEmpty()) {
            return List.of();
        }

        QMenu menu = QMenu.menu;  // 추가

        return queryFactory
                .selectDistinct(Projections.constructor(StoreListQueryResult.class,
                        store.id,
                        store.name,
                        store.status,
                        QueryExpressionFormatter.getImageKeyPath(storeImage.imageKey),
                        store.maxPercent,
                        QueryExpressionFormatter.roundDoubleByFirstDecimalPlace(review.rating.avg()),
                        review.countDistinct(),
                        store.openTime,
                        store.closeTime))
                .from(store)
                .leftJoin(store.storeImages, storeImage)
                .leftJoin(store.reviews, review)
                .leftJoin(store.menus, menu)  // 메뉴 조인 추가
                .where(
                        store.isDeleted.eq(false),
                        store.id.in(storeIds),
                        eqCategory(category),
                        eqKeyword(keyword))
                .groupBy(store.id, store.name, store.address, store.category)
                .fetch();
    }

    /**
     * 가게 ID로 조회
     */
    @Override
    public Optional<Store> findByStoreId(Long storeId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(store)
                        .where(
                                store.isDeleted.eq(false),
                                store.id.eq(storeId)
                        )
                        .fetchOne()
        );
    }

    /**
     * 찜한 가게 목록 조회 (단방향 무한 스크롤 전용)
     */
    @Override
    public List<StoreListQueryResult> findFavoriteStoresForward(long memberId, SortType sort, long cursorId, int limit) {
        JPAQuery<StoreListQueryResult> query = queryFactory
                .select(Projections.constructor(StoreListQueryResult.class,
                        store.id,
                        store.name,
                        store.status,
                        QueryExpressionFormatter.getImageKeyPath(storeImage.imageKey),
                        store.maxPercent,
                        QueryExpressionFormatter.roundDoubleByFirstDecimalPlace(review.rating.avg()),
                        review.countDistinct(),
                        store.openTime,
                        store.closeTime)
                )
                .from(store)
                .leftJoin(store.favorites, favorite).where(favorite.member.id.eq(memberId))
                .leftJoin(store.reviews, review)
                .leftJoin(store.storeImages, storeImage)
                .where(
                        store.isDeleted.eq(false),
                        gtFavoriteCursorId(cursorId)
                )
                .groupBy(store.id, favorite.id);

        // 정렬 방식 적용
        if (sort == null || sort == SortType.REVIEW) {
            query.orderBy(
                    store.status.desc(),  // OPEN 우선
                    favorite.id.asc()     // 찜한 순서
            );
        } else if (sort == SortType.RATING) {
            // 평점순
            query.orderBy(
                    store.status.desc(),  // OPEN 우선
                    QueryExpressionFormatter.roundDoubleByFirstDecimalPlace(review.rating.avg()).desc().nullsLast(),
                    favorite.id.asc()     // 동점시 찜한 순서
            );
        } else if (sort == SortType.NEAR) {
            // 거리순
            query.orderBy(
                    store.status.desc(),  // OPEN 우선
                    favorite.id.asc()     // 찜한 순서
            );
        }

        return query.limit(limit).fetch();
    }

    /**
     * 찜한 가게용 커서 조건 (favorite.id 기준)
     */
    private static BooleanExpression gtFavoriteCursorId(Long cursorId) {
        if (cursorId == null || cursorId == 0) {
            return null;
        } else {
            return favorite.id.gt(cursorId);  // favorite.id > cursorId
        }
    }

    private BooleanExpression eqCategory(Category category) {
        return category != null ? store.category.eq(category) : null;
    }

    private BooleanExpression eqKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        QMenu menu = QMenu.menu;

        return store.name.containsIgnoreCase(keyword)
                .or(menu.name.containsIgnoreCase(keyword).and(menu.isDeleted.eq(false)));
    }

    private static BooleanExpression gtCursorId(Long cursorId) {
        if (cursorId == null || cursorId == 0) {
            return null;
        } else {
            return store.id.gt(cursorId);
        }
    }

    private static BooleanExpression ltCursorId(Long cursorId) {
        if (cursorId == null || cursorId == 0) {
            return null;
        } else {
            return store.id.lt(cursorId);
        }
    }

    private static BooleanExpression goeCursorId(Long cursorId) {
        if (cursorId == null || cursorId == 0) {
            return null;
        } else {
            return store.id.goe(cursorId);
        }
    }

    /**
     * 커서 조건 생성
     */
    private BooleanExpression getCursorCondition(Long cursorId, SortType sort, boolean isNext) {
        if (cursorId == null || cursorId == 0) {
            return null;
        }

        return isNext ? store.id.gt(cursorId) : store.id.lt(cursorId);
    }

    /**
     * 정렬 조건 생성
     */
    private OrderSpecifier<?>[] getOrderSpecifiers(SortType sort, boolean isNext) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        // 항상 OPEN 상태를 우선
        orderSpecifiers.add(store.status.desc());

        if (sort == null) {
            // 기본: ID 기준
            orderSpecifiers.add(isNext ? store.id.asc() : store.id.desc());
        } else {
            switch (sort) {
                case NEAR -> {
                    // 거리순은 Redis GEO에서 처리하므로 여기서는 ID만
                    orderSpecifiers.add(isNext ? store.id.asc() : store.id.desc());
                }
                case REVIEW -> {
                    // 리뷰 많은 순 (내림차순) + ID
                    if (isNext) {
                        orderSpecifiers.add(review.countDistinct().desc());
                        orderSpecifiers.add(store.id.asc());
                    } else {
                        orderSpecifiers.add(review.countDistinct().asc());
                        orderSpecifiers.add(store.id.desc());
                    }
                }
                case RATING -> {
                    // 평점 높은 순 (내림차순) + ID
                    if (isNext) {
                        orderSpecifiers.add(review.rating.avg().desc());
                        orderSpecifiers.add(store.id.asc());
                    } else {
                        orderSpecifiers.add(review.rating.avg().asc());
                        orderSpecifiers.add(store.id.desc());
                    }
                }
            }
        }

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

    /**
     * 메모리에서 정렬 (direction=false일 때 사용)
     */
    private List<StoreListQueryResult> applySorting(List<StoreListQueryResult> results, SortType sort) {
        if (sort == null) {
            return results.stream()
                    .sorted(Comparator.comparing(StoreListQueryResult::id))
                    .collect(Collectors.toList());
        }

        return switch (sort) {
            case NEAR -> results.stream()
                    .sorted(Comparator.comparing(StoreListQueryResult::id))
                    .collect(Collectors.toList());
            case REVIEW -> results.stream()
                    .sorted(Comparator.comparing(StoreListQueryResult::count, Comparator.reverseOrder())
                            .thenComparing(StoreListQueryResult::id))
                    .collect(Collectors.toList());
            case RATING -> results.stream()
                    .sorted(Comparator.comparing(StoreListQueryResult::ratingAvg,
                                    Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(StoreListQueryResult::id))
                    .collect(Collectors.toList());
        };
    }
}