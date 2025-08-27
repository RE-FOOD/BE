package com.iitp.domains.map.repository;

import com.iitp.domains.store.domain.entity.QStore;
import com.iitp.domains.store.domain.entity.QStoreImage;
import com.iitp.domains.store.domain.entity.Store;
import com.querydsl.core.types.dsl.BooleanExpression;
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

    // 커서 기반 페이징으로 상점 조회
    @Override
    public List<Store> findStoreListByIdsWithCursor(List<Long> storeIds, Long cursorId, Integer limit) {
        return queryFactory
                .selectFrom(store)
                .leftJoin(store.storeImages, storeImage).fetchJoin()
                .where(
                        store.id.in(storeIds),
                        store.isDeleted.eq(false),
                        cursorCondition(cursorId)
                )
                .orderBy(store.id.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * 커서 조건 생성
     * cursorId가 null이면 처음부터, 있으면 해당 ID보다 큰 값부터
     */
    private BooleanExpression cursorCondition(Long cursorId) {
        return cursorId != null ? store.id.gt(cursorId) : null;
    }
}