package com.iitp.domains.review.repository;

import com.iitp.domains.review.domain.entity.Review;
import com.iitp.domains.review.repository.mapper.ReviewAggregationResult;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepositoryCustom {
    List<Review> findReviewsByStore(long storeId, long cursorId, int limit);

    Optional<ReviewAggregationResult> findReviewRatingAverageByStore(long storeId);

    List<Review> findReviewsByMember(long memberId, long cursorId, int limit);
}
