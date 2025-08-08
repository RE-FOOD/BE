package com.iitp.domains.store.repository.store;

import com.iitp.domains.store.domain.entity.StoreImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {

}
