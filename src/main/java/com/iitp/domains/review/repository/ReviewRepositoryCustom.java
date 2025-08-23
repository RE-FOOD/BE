package com.iitp.domains.review.repository;

import com.iitp.domains.review.domain.entity.Review;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepositoryCustom {
    public List<Review> findReviewsByStore(long storeId, long cursorId, int limit);

}
