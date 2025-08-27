package com.iitp.domains.store.repository.store;

import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.SortType;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.mapper.StoreListQueryResult;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepositoryCustom {
    Optional<Store> findByStoreId(Long storeId);

    // 커서 기반 페이징 (리뷰순, 평점순)
    List<StoreListQueryResult> findStores(Category category, String keyword, SortType sort, Long cursorId,
                                          boolean direction, int limit);

    // 찜한 가게 목록 조회
    List<StoreListQueryResult> findFavoriteStores(long memberId, SortType sort, long cursorId, int limit);

    // Redis GEO 기반 거리순 정렬을 위한 메서드
    List<StoreListQueryResult> findStoresByIds(List<Long> storeIds, Category category, String keyword);
}