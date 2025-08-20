package com.iitp.domains.store.repository.store;

import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.SortType;
import com.iitp.domains.store.domain.StoreStatus;
import com.iitp.domains.store.domain.entity.QStore;
import com.iitp.domains.store.domain.entity.QStoreImage;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.mapper.StoreListQueryResult;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;
    private static final QStore store = QStore.store;
    QStoreImage storeImage = QStoreImage.storeImage;


    @Override
    public List<StoreListQueryResult> findStores(Category category, String keyword, SortType sort, Long cursorId, boolean direction, int limit) {

        // direction = true: 다음 페이지 (9, 10, 11, ..., 18)
        if (direction) {
            return queryFactory
                    .selectDistinct(Projections.constructor(StoreListQueryResult.class,
                            store.id,
                            store.name,
                            store.status,
                            Expressions.stringTemplate("MIN({0})", storeImage.imageKey),
                            store.maxPercent,
                            store.openTime,
                            store.closeTime))
                    .from(store)
                    .leftJoin(store.storeImages, storeImage)
                    .where(
                            store.isDeleted.eq(false),
                            cursorId != null ? store.id.gt(cursorId) : null,  // cursorId보다 큰 ID
                            eqCategory(category),
                            eqKeyword(keyword)
                    )
                    .groupBy(store.id, store.name, store.address, store.category)
                    .orderBy(
                            store.status.desc(),
                            store.id.asc()  // ID 오름차순 정렬
                    )
                    .limit(limit)
                    .fetch();
        }
        // direction = false: 이전 페이지 (cursorId 이전의 값들 + 부족하면 cursorId 이후도 포함)
        else{
            // 1단계: cursorId 이전의 값들을 먼저 조회
            List<StoreListQueryResult> beforeResults = queryFactory
                    .selectDistinct(Projections.constructor(StoreListQueryResult.class,
                            store.id,
                            store.name,
                            store.status,
                            Expressions.stringTemplate("MIN({0})", storeImage.imageKey),
                            store.maxPercent,
                            store.openTime,
                            store.closeTime))
                    .from(store)
                    .leftJoin(store.storeImages, storeImage)
                    .where(
                            store.isDeleted.eq(false),
                            cursorId != null ? store.id.lt(cursorId) : null,  // cursorId보다 작은 ID
                            eqCategory(category),
                            eqKeyword(keyword)
                    )
                    .groupBy(store.id, store.name, store.address, store.category)
                    .orderBy(
                            store.status.desc(),
                            store.id.desc()  // ID 내림차순 정렬
                    )
                    .limit(limit)
                    .fetch();

            // 2단계: cursorId 이전의 값들이 limit에 못 미치면, cursorId 이후의 값들도 조회
            if (beforeResults.size() < limit) {
                int remainingLimit = limit - beforeResults.size();

                List<StoreListQueryResult> afterResults = queryFactory
                        .selectDistinct(Projections.constructor(StoreListQueryResult.class,
                                store.id,
                                store.name,
                                store.status,
                                Expressions.stringTemplate("MIN({0})", storeImage.imageKey),
                                store.maxPercent,
                                store.openTime,
                                store.closeTime))
                        .from(store)
                        .leftJoin(store.storeImages, storeImage)
                        .where(
                                store.isDeleted.eq(false),
                                cursorId != null ? store.id.goe(cursorId) : null,  // cursorId 이상의 ID
                                eqCategory(category),
                                eqKeyword(keyword)
                        )
                        .groupBy(store.id, store.name, store.address, store.category)
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



    private BooleanExpression eqWriterId(Long userId) {
        return userId != null ? store.memberId.eq(userId) : null;
    }

    private BooleanExpression eqCategory(Category category) {
        return category != null ? store.category.eq(category) : null;
    }

    private BooleanExpression eqKeyword(String keyword) {
        return keyword != null ? store.origin.containsIgnoreCase(keyword).or(store.name.containsIgnoreCase(keyword)) : null;
    }

    private BooleanExpression validCursorId(Long cursorId) {
        return cursorId!=null ? store.id.gt(cursorId) : null;
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
