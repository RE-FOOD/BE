package com.iitp.domains.store.repository.menu;

import com.iitp.domains.store.domain.entity.*;
import com.iitp.domains.store.repository.mapper.MenuListQueryResult;
import com.querydsl.core.types.Projections;
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
                        menu.imageKey))
                .from(menu)
                .where(
                        menu.store.id.eq(storeId),
                        menu.isDeleted.eq(false)
                )
                .fetch();
    }
}
