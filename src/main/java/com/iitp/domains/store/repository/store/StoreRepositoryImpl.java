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
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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

    @Override
    public List<StoreListQueryResult> findStores(Category category, String keyword, SortType sort, Long cursorId,
                                                 boolean direction, int limit) {

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
                .where(
                        store.isDeleted.eq(false),
                        eqCategory(category),
                        eqKeyword(keyword))
                .groupBy(store.id, store.name, store.address, store.category);

        // direction = true: 다음 페이지 (9, 10, 11, ..., 18). 오름차순 정렬
        // direction = false: 이전 페이지 (cursorId 이전의 값들 + 부족하면 cursorId 이후도 포함). 내림차순 정렬
        if (direction) {
            return baseQuery
                    .where(gtCursorId(cursorId))
                    .orderBy(
                            store.status.desc(),
                            store.id.asc())
                    .limit(limit)
                    .fetch();
        } else {
            // 1단계: cursorId 이전의 값들을 먼저 조회
            List<StoreListQueryResult> beforeResults = baseQuery
                    .where(ltCursorId(cursorId))  // cursorId보다 작은 ID)
                    .orderBy(
                            store.status.desc(),
                            store.id.desc()  // ID 내림차순 정렬
                    )
                    .limit(limit)
                    .fetch();

            // 2단계: cursorId 이전의 값들이 limit에 못 미치면, cursorId 이후의 값들도 조회
            if (beforeResults.size() < limit) {
                int remainingLimit = limit - beforeResults.size();

                List<StoreListQueryResult> afterResults = baseQuery
                        .where(goeCursorId(cursorId))
                        .orderBy(
                                store.status.desc(),
                                store.id.asc()  // ID 오름차순 정렬
                        )
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
    public List<StoreListQueryResult> findFavoriteStores(long memberId, SortType sort, long cursorId, int limit) {

        // 정렬 방법에 따라서 cursorId 다르게 지정
        // 기본 -> ORDER BY favorite.id DESC
        // TODO: 정렬 방법에 따라 다르게 정렬
        // NEAR -> 현재 회원의 위도경도 비교해서
        // TODO: 위도 경도 비교 어떻게? SQL로 해결할 수 있는 문제인지 아니면...
        // REVIEW -> review 수에 따라서 정렬
        // RATING -> review rating에 따라서 정렬
        // store.openTime, closeTime -> Status 여기서 결정해버리기
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
                        store.closeTime)
                )
                .from(store)
                .leftJoin(store.favorites, favorite).where(favorite.member.id.eq(memberId))
                .leftJoin(store.reviews, review)
                .leftJoin(store.storeImages, storeImage)
                .where(
                        store.isDeleted.eq(false),
                        ltCursorId(cursorId)
                )
                .orderBy(
                        store.status.desc(),
                        favorite.id.desc()
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

    private OrderSpecifier<?> getOrderSpecifier(String sort) {
        Order order = Order.DESC;

        if (Objects.isNull(sort)) {
            return new OrderSpecifier<>(order, store.id);
        }

        return switch (sort) {
            case "NEAR" -> new OrderSpecifier<>(order, store.createdAt);
            case "REVIEW" -> new OrderSpecifier<>(order, review.countDistinct());
            case "RATING" -> new OrderSpecifier<>(order, review.rating.avg());
            default -> new OrderSpecifier<>(order, store.id);
        };
    }
}
