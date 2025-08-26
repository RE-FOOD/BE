package com.iitp.domains.store.repository.store;

import static com.iitp.domains.favorite.domain.entity.QFavorite.favorite;
import static com.iitp.domains.review.domain.entity.QReview.review;
import static com.iitp.domains.store.domain.entity.QStore.store;
import static com.iitp.domains.store.domain.entity.QStoreImage.storeImage;

import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.SortType;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.mapper.StoreListQueryResult;
import com.iitp.global.util.query.QueryExpressionFormatter;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    // TODO fix: direction FALSE 일 경우 레코드가 2개씩 중복해서 나오는 문제. 그렇다고 distinct를 냅두면 에러.."[Expression #1 of ORDER BY clause is not in SELECT list, contains aggregate function; this is incompatible with DISTINCT] [n/a]"
    @Override
    public List<StoreListQueryResult> findStores(
            Category category, String keyword, SortType sort, boolean direction, int limit,
            Long cursorId, Double cursorDistance, Double cursorReviewAvg, Long cursorReviewCnt,
            double latitude, double longitude
    ) {
        NumberExpression<Double> distance = distanceKmRoundedFirst(latitude, longitude);

        // 서브쿼리: 가게 대표 이미지 1개
        Expression<String> minImageKey =
                JPAExpressions
                        .select(storeImage.imageKey.min())
                        .from(storeImage)
                        .where(storeImage.store.eq(store));

        // 공통 쿼리: 검색, 카테고리 필터링, 리뷰 집계, 정렬 방식에 따른 커서 페이지네이션 및 정렬 분기 처리
        JPAQuery<StoreListQueryResult> baseQuery = queryFactory
                .select(Projections.constructor(StoreListQueryResult.class,
                                store.id,
                                store.name,
                                store.status,
                                minImageKey,
                                store.maxPercent,
                                QueryExpressionFormatter.roundDoubleByFirstDecimalPlace(review.rating.avg()),
                                review.id.countDistinct(),
                                store.openTime,
                                store.closeTime,
                                distance
                        )
                ).from(store)
                .leftJoin(store.reviews, review)
                .where(
                        store.isDeleted.isFalse(),
                        eqCategory(category),
                        eqKeyword(keyword),
                        specifySortCursor(sort, distance, cursorDistance, cursorReviewAvg, cursorReviewCnt)
                ).groupBy(store.id, store.status)
                .orderBy(specifySortOrder(sort, distance), store.status.desc());

        // direction = true: 다음 페이지 (9, 10, 11, ..., 18). 내림차순 정렬
        // direction = false: 이전 페이지 (cursorId 이전의 값들 + 부족하면 cursorId 이후도 포함). 내림차순 정렬
        if (direction) {
            return baseQuery
                    .where(ltCursorId(cursorId))
                    .orderBy(store.id.desc())
                    .limit(limit)
                    .fetch();
        } else {
            // 1단계: cursorId 이전의 값들을 먼저 조회
            List<StoreListQueryResult> beforeResults = baseQuery
                    .where(ltCursorId(cursorId))  // cursorId보다 작은 ID)
                    .orderBy(store.id.desc())
                    .limit(limit)
                    .fetch();

            // 2단계: cursorId 이전의 값들이 limit에 못 미치면, cursorId 이후의 값들도 조회
            if (beforeResults.size() < limit) {
                int remainingLimit = limit - beforeResults.size();

                List<StoreListQueryResult> afterResults = baseQuery
                        .where(goeCursorId(cursorId))
                        .orderBy(store.id.asc())
                        .limit(remainingLimit)
                        .fetch();

                // 3단계: 두 결과를 합치고 ID 오름차순으로 정렬
                List<StoreListQueryResult> combinedResults = new ArrayList<>();
                combinedResults.addAll(beforeResults);
                combinedResults.addAll(afterResults);

                return combinedResults.stream()
                        .sorted((a, b) -> a.id().compareTo(b.id()))  // ID 오름차순으로 정렬
                        .collect(Collectors.toList());
            }

            // cursorId 이전의 값들만으로 limit를 채운 경우
            return beforeResults.stream()
                    .sorted((a, b) -> a.id().compareTo(b.id()))  // ID 오름차순으로 정렬
                    .collect(Collectors.toList());
        }
    }

//    @Override
//    public List<StoreListQueryResult> findStores(Category category, String keyword, SortType sort, Long cursorId, boolean direction, int limit) {
//
//        return queryFactory
//                .selectDistinct(Projections.constructor(StoreListQueryResult.class,
//                        store.id,
//                        store.name,
//                        store.status,
//                        Expressions.stringTemplate("MIN({0})", storeImage.imageKey),        // MIN으로 첫 번째 이미지
//                        store.maxPercent,
//                        store.openTime,
//                        store.closeTime))
//                .from(store)
//                .leftJoin(store.storeImages, storeImage)
//                .where(
//                        store.isDeleted.eq(false),
//                        validCursorId(cursorId),
//                        eqCategory(category),
//                        eqKeyword(keyword)
//                )
//                .groupBy(store.id, store.name, store.address, store.category) // storeId별로 그룹화
//                // TODO :: Sort 기준 필터 추가 (리뷰 연동되면)
////                .orderBy(getOrderSpecifier(sort, sortAsc))
//                .orderBy(
//                        store.status.desc(),
//                        store.id.asc()       // 같은 상태 내에서는 ID 순
//                )
//                .limit(limit)
//                .fetch();
//    }

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

    @Override
    public List<StoreListQueryResult> findFavoriteStores(
            long memberId, SortType sort, int limit,
            long cursorId, Double cursorDistance, Double cursorReviewAvg, Long cursorReviewCnt,
            double latitude, double longitude
    ) {
        NumberExpression<Double> distance = distanceKmRoundedFirst(latitude, longitude);
        List<StoreListQueryResult> resultList = queryFactory
                .select(Projections.constructor(StoreListQueryResult.class,
                        store.id,
                        store.name,
                        store.status,
                        QueryExpressionFormatter.getImageKeyPath(storeImage.imageKey),
                        store.maxPercent,
                        QueryExpressionFormatter.roundDoubleByFirstDecimalPlace(review.rating.avg()),
                        review.countDistinct(),
                        store.openTime,
                        store.closeTime,
                        distance)
                )
                .from(store)
                .leftJoin(store.favorites, favorite).where(favorite.member.id.eq(memberId))
                .leftJoin(store.reviews, review)
                .leftJoin(store.storeImages, storeImage)
                .where(
                        store.isDeleted.isFalse(),
                        review.isDeleted.isFalse(),
                        specifySortCursor(sort, distance, cursorDistance, cursorReviewAvg, cursorReviewCnt),
                        ltCursorId(cursorId)
                )
                .orderBy(
                        specifyOrderBySortTypeDefaultFavorite(sort, distance),
                        store.id.desc(),
                        store.status.desc()
                )
                .groupBy(store.id, favorite.id)
                .limit(limit)
                .fetch();

        return resultList;
    }

    private BooleanExpression eqWriterId(Long userId) {
        return userId != null ? store.memberId.eq(userId) : null;
    }

    private BooleanExpression eqCategory(Category category) {
        return category != null ? store.category.eq(category) : null;
    }

    private BooleanExpression eqKeyword(String keyword) {
        return keyword != null
                ? store.origin.containsIgnoreCase(keyword).or(store.name.containsIgnoreCase(keyword))
                : null;
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

    private static BooleanExpression specifySortCursor(
            SortType sort,
            NumberExpression<Double> distance,
            Double cursorDistance,
            Double cursorReviewAvg,
            Long cursorReviewCnt
    ) {
        if (sort == null) {
            return null;
        }
        return switch (sort) {
            case NEAR -> Objects.isNull(cursorDistance) ? null :distance.goe(cursorDistance);
            case REVIEW -> Objects.isNull(cursorReviewCnt) ? null : review.rating.countDistinct().loe(cursorReviewCnt);
            case RATING -> Objects.isNull(cursorReviewAvg) ? null : review.rating.avg().loe(cursorReviewAvg);
        };
    }

    private static OrderSpecifier<?> specifySortOrder(SortType sort, NumberExpression<Double> distance) {
        if (Objects.isNull(sort)) {
            return null;
        }

        return switch (sort) {
            // distance ASC, cursorId DESC
            case SortType.NEAR -> new OrderSpecifier<>(Order.ASC, distance);
            case SortType.REVIEW -> new OrderSpecifier<>(Order.DESC, review.countDistinct());
            case SortType.RATING -> new OrderSpecifier<>(Order.DESC, review.rating.avg());
        };
    }


    private OrderSpecifier<?> specifyOrderBySortTypeDefaultFavorite(
            SortType sort,
            NumberExpression<Double> distance
    ) {
        if (Objects.isNull(sort)) {
            return new OrderSpecifier<>(Order.DESC, favorite.id);
        }

        return switch (sort) {
            case SortType.NEAR -> new OrderSpecifier<>(Order.ASC, distance);
            case SortType.REVIEW -> new OrderSpecifier<>(Order.DESC, review.countDistinct());
            case SortType.RATING -> new OrderSpecifier<>(Order.DESC, review.rating.avg());
        };
    }

    private BooleanExpression getCursorIdOrderBySortType(SortType sort, Long cursorId) {
        if (Objects.isNull(sort)) {
            return ltCursorId(cursorId);
        }

        return switch (sort) {
            case SortType.NEAR -> gtCursorId(cursorId);
            case SortType.REVIEW, SortType.RATING -> ltCursorId(cursorId);
        };

    }


    // MYSQL 기능 활용. 소수점3째자리까지 반올림
    private NumberExpression<Double> distanceKmRoundedFirst(double userLat, double userLon) {
        return Expressions.numberTemplate(
                Double.class,
                "round(cast(function('ST_Distance_Sphere', point({0},{1}), point({2},{3})) as double) / 1000.0, 3)",
                store.longitude, store.latitude, userLon, userLat
        );
    }
}
