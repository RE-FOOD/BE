package com.iitp.domains.map.repository;

import com.iitp.domains.store.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapRepository extends JpaRepository<Store, Long>, MapRepositoryCustom {
}
