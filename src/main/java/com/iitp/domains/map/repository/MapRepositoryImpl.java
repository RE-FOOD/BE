package com.iitp.domains.map.repository;

import com.iitp.domains.store.domain.entity.QStore;
import com.iitp.domains.store.domain.entity.QStoreImage;
import com.iitp.domains.store.domain.entity.Store;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MapRepositoryImpl implements MapRepositoryCustom {
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;
    private static final QStore store = QStore.store;
    private static final QStoreImage storeImage = QStoreImage.storeImage;


    // 모든 상점 조회
    @Override
    public List<Store> findStoreListByIds(List<Long> storeIds) {
        return queryFactory
                .selectFrom(store)
                .leftJoin(store.storeImages, storeImage).fetchJoin()
                .where(
                        store.id.in(storeIds),
                        store.isDeleted.eq(false)
                )
                .fetch();
    }
    // 영업중인 상점만 조회
    @Override
    public List<Store> findActiveStoreListByIds(List<Long> storeIds) {
        return queryFactory
                .selectFrom(store)
                .leftJoin(store.storeImages, storeImage).fetchJoin()
                .where(
                        store.id.in(storeIds),
                        store.isDeleted.eq(false)
                )
                .fetch();
    }
}