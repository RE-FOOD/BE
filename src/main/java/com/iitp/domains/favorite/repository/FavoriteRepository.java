package com.iitp.domains.favorite.repository;

import com.iitp.domains.favorite.domain.entity.Favorite;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByMemberIdAndStoreId(Long memberId, Long storeId);
    boolean existsByMemberIdAndStoreId(Long memberId, Long storeId);
}
