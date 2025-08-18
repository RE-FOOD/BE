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
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;
    private static final QStore store = QStore.store;
    QStoreImage storeImage = QStoreImage.storeImage;

    @Override
    public List<StoreListQueryResult> findStores(Category category, String keyword, SortType sort, Long cursorId, int limit) {

        return queryFactory
                .selectDistinct(Projections.constructor(StoreListQueryResult.class,
                        store.id,
                        store.name,
                        store.status,
                        Expressions.stringTemplate("MIN({0})", storeImage.imageKey),
                        store.maxPercent)) // MIN으로 첫 번째 이미지
                .from(store)
                .leftJoin(store.storeImages, storeImage)
                .where(
                        store.isDeleted.eq(false),
                        store.openTime.after(LocalTime.now()),
//                        store.status.eq(StoreStatus.OPEN), // 영업중인 매장만
                        eqCategory(category),
                        eqKeyword(keyword),
                        validCursorId(cursorId)
                )
                .groupBy(store.id, store.name, store.address, store.category) // storeId별로 그룹화
                // TODO :: Sort 기준 필터 추가 (리뷰 연동되면)
//                .orderBy(getOrderSpecifier(sort, sortAsc))
                .orderBy(
                        store.status.desc(),
                        store.id.asc()       // 같은 상태 내에서는 ID 순
                )
                .limit(limit)
                .fetch();
    }

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
