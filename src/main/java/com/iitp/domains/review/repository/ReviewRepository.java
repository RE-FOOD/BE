package com.iitp.domains.review.repository;

import com.iitp.domains.review.domain.entity.Review;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
    Boolean existsByMemberIdAndOrderId(Long memberId, Long orderId);
}
