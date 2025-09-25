package com.iitp.domains.review.service.query;

import com.iitp.domains.cart.domain.entity.CartMenu;
import com.iitp.domains.cart.service.query.CartQueryService;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.domains.review.domain.entity.Review;
import com.iitp.domains.review.dto.response.MyReviewResponse;
import com.iitp.domains.review.dto.response.ReviewResponse;
import com.iitp.domains.review.repository.ReviewRepository;
import com.iitp.domains.review.repository.mapper.ReviewAggregationResult;
import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.domains.store.service.query.MenuQueryService;
import com.iitp.domains.store.service.query.StoreQueryService;
import com.iitp.domains.store.validator.StoreValidator;
import com.iitp.global.common.response.TwoWayCursorListResponse;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


public interface ReviewQueryService {

    /**
     * API 응답 메서드
     */
    TwoWayCursorListResponse<ReviewResponse> readStoreReviews(Long storeId, Long cursorId, int limit);

    TwoWayCursorListResponse<MyReviewResponse> readMyReviews(long memberId, long cursorId, int limit);

    /**
     * 조회 및 DTO 변환
     */
    List<ReviewResponse> getStoreReviews(long storeId, Long cursorId, int limit);

    List<MyReviewResponse> getMyReviews(long authorId, Long cursorId, int limit);

    /**
     * 공통 메서드
     */

    Double calculateStoreRating(Long storeId);


    List<Menu> getOrderedMenusOfReview(Review review);


    Optional<ReviewAggregationResult> findReviewRatingAverageByStore(Long storeId);

}
