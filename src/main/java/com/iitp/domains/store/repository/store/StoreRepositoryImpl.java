package com.iitp.domains.store.repository.store;

import static com.iitp.domains.favorite.domain.entity.QFavorite.favorite;
import static com.iitp.domains.review.domain.entity.QReview.review;

import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.SortType;
import com.iitp.domains.store.domain.entity.QStore;
import com.iitp.domains.store.domain.entity.QStoreImage;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.mapper.StoreListQueryResult;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;
    private static final QStore store = QStore.store;
    QStoreImage storeImage = QStoreImage.storeImage;


    @Override
    public List<StoreListQueryResult> findStores(Category category, String keyword, SortType sort, Long cursorId,
                                                 boolean direction, int limit) {

        JPAQuery<StoreListQueryResult> baseQuery = queryFactory
                .selectDistinct(Projections.constructor(StoreListQueryResult.class,
                        store.id,
                        store.name,
                        store.status,
                        getImageKeyPath(storeImage.imageKey),
                        store.maxPercent,
                        review.rating.avg(),
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


    private com.querydsl.core.types.dsl.StringTemplate getImageKeyPath(StringPath path) {
        return Expressions.stringTemplate("MIN({0})", path);
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
        // REVIEW -> review 수에 따라서 정렬
        // RATING -> review rating에 따라서 정렬
        List<Store> stores = queryFactory.selectFrom(store)
                .leftJoin(store.favorites, favorite)
                .leftJoin(store.reviews, review)
                .leftJoin(store.storeImages, storeImage)
                .where(favorite.member.id.eq(memberId))
                .where(ltCursorId(cursorId))
                .orderBy(favorite.id.desc())
                .limit(limit)
                .fetch();

        stores.forEach(it -> System.out.println("it.getName() = " + it.getName()));

        // review.rating.avg(), review.count()
        // store.openTime, closeTime -> Status 여기서 결정해버리기
        List<StoreListQueryResult> resultList = queryFactory
                .select(Projections.constructor(StoreListQueryResult.class,
                        store.id,
                        store.name,
                        store.status,
                        getImageKeyPath(storeImage.imageKey),
                        store.maxPercent,
                        review.rating.avg(),
                        review.countDistinct(),
                        store.openTime,
                        store.closeTime)
                )
                .from(store)
                .leftJoin(store.favorites, favorite)
                .leftJoin(store.reviews, review)
                .leftJoin(store.storeImages, storeImage)
                .where(
                        store.isDeleted.eq(false),
                        favorite.member.id.eq(memberId),
                        ltCursorId(cursorId)
                )
                .orderBy(
                        store.status.desc(),
                        favorite.id.desc()  // 찜한 순서대로 ID 내림차순 정렬
                )
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

//    private OrderSpecifier<?> getOrderSpecifier(String sort) {
//        Order order =  Order.ASC ;
//
//        if (sort == null) {
//            return new OrderSpecifier<>(order, store.createdAt);
//        }
//
//
//        return switch (sort) {
//            case "NEAR" -> new OrderSpecifier<>(order, store.createdAt);
//            case "REVIEW" -> new OrderSpecifier<>(order, store.deadline);
//            case "RATING" -> new OrderSpecifier<>(order, store.currentPerson);
//            default -> new OrderSpecifier<>(order, store.createdAt);
//        };
//    }
}
