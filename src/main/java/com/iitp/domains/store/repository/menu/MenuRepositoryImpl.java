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
}
