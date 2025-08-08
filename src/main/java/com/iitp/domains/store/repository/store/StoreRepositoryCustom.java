package com.iitp.domains.store.repository.store;

import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.SortType;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.dto.response.StoreListResponse;
import com.iitp.domains.store.repository.mapper.StoreListQueryResult;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepositoryCustom {
    Optional<Store> findByStoreId(Long storeId);
    List<StoreListQueryResult> findStores(Category category, String keyword, SortType sort, Long cursorId, int limit);
}
