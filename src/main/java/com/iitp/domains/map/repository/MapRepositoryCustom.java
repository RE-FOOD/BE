package com.iitp.domains.map.repository;

import com.iitp.domains.store.domain.entity.Store;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MapRepositoryCustom {
    //ID 리스트로 가게 조회 (이미지 포함)
    List<Store> findStoreListByIds(List<Long> storeIds);
    // 영업 중인 가게만 ID 리스트로 조회
    List<Store> findActiveStoreListByIds(List<Long> storeIds);
}