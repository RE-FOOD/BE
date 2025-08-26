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

    List<StoreListQueryResult> findStores(Category category, String keyword, SortType sort, Long cursorId,
                                          boolean direction, int limit);

    List<StoreListQueryResult> findFavoriteStores(long memberId, SortType sort, long cursorId, int limit);
}
