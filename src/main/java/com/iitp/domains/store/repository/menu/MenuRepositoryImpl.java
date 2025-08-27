package com.iitp.domains.store.repository.menu;

import com.iitp.domains.store.domain.entity.*;
import com.iitp.domains.store.repository.mapper.MenuListQueryResult;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom {
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;
    private static final QMenu menu = QMenu.menu;
    QStoreImage storeImage = QStoreImage.storeImage;


    @Override
    public Optional<Menu> findByMenuId(Long menuId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(menu)
                        .where(
                                menu.isDeleted.eq(false),
                                menu.id.eq(menuId)
                        )
                        .fetchOne()
        );
    }

    @Override
    public List<MenuListQueryResult> findAllMenu(Long storeId) {
        return queryFactory
                .selectDistinct(Projections.constructor(MenuListQueryResult.class,
                        menu.id,
                        menu.name,
                        menu.price,
                        menu.dailyDiscountPercent,
                        menu.dailyQuantity,
                        menu.discountPrice,
                        menu.imageKey))
                .from(menu)
                .where(
                        menu.store.id.eq(storeId),
                        menu.isDeleted.eq(false)
                )
                .orderBy(
                        // 1순위: 재고 상태 (있음 > 없음)
                        getQuantityOrderExpression(menu),
                        // 2순위: 메뉴 이름 순
                        menu.name.asc()
                )
                .fetch();
    }


    // 재고 상태별 정렬 표현식
    private OrderSpecifier<?> getQuantityOrderExpression(QMenu menu) {
        return new CaseBuilder()
                .when(menu.dailyQuantity.gt(0)).then(1)  // 재고 있음: 1
                .otherwise(2)                        // 재고 없음: 2
                .asc();                              // 오름차순 (1이 먼저, 2가 나중에)
    }

    // 할인 메뉴 조회 메서드
    @Override
    public List<Menu> findDiscountMenusByStoreIds(List<Long> storeIds, int limit) {
        QMenu menu = QMenu.menu;
        QStore store = QStore.store;

        if (storeIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(menu)
                .join(menu.store, store).fetchJoin()
                .where(
                        store.id.in(storeIds)
                                .and(menu.dailyDiscountPercent.gt(0))  // 할인율이 0보다 큰 메뉴
                                .and(menu.dailyQuantity.gt(0))         // 재고가 있는 메뉴
                                .and(store.isDeleted.eq(false))        // 삭제되지 않은 가게
                                .and(menu.isDeleted.eq(false))         // 삭제되지 않은 메뉴
                )
                .orderBy(menu.dailyDiscountPercent.desc())     // 할인율 높은 순
                .limit(limit)
                .fetch();
    }

}
