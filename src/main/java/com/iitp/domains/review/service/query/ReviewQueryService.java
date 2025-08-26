package com.iitp.domains.review.service.query;

import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.domains.review.domain.entity.Review;
import com.iitp.domains.review.dto.response.ReviewResponse;
import com.iitp.domains.review.repository.ReviewRepository;
import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.service.query.StoreQueryService;
import com.iitp.global.common.response.TwoWayCursorListResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReviewQueryService {
    private final ReviewRepository reviewRepository;
    private final MemberQueryService memberQueryService;
    private final StoreQueryService storeQueryService;

    /**
     * API 응답 메서드
     */
    public TwoWayCursorListResponse<ReviewResponse> readStoreReviews(Long storeId, Long cursorId, int limit) {
        Store store = storeQueryService.findExistingStore(storeId);
        List<ReviewResponse> result = getStoreReviews(store.getId(), cursorId, limit);
        return new TwoWayCursorListResponse<>(result.getFirst().id(), result.getLast().id(), result);
    }

    public TwoWayCursorListResponse<ReviewResponse> readMyReviews(long memberId, long cursorId, int limit) {
        Member author = memberQueryService.findMemberById(memberId);
        List<ReviewResponse> result = getMyReviews(author.getId(), cursorId, limit);
        return new TwoWayCursorListResponse<>(result.getFirst().id(), result.getLast().id(), result);
    }

    /**
     * 조회 및 DTO 변환
     */
    public List<ReviewResponse> getStoreReviews(long storeId, Long cursorId, int limit) {
        List<Review> reviews = reviewRepository.findReviewsByStore(storeId, cursorId, limit);
        return convertToResponse(reviews);
    }

    private List<ReviewResponse> getMyReviews(long authorId, Long cursorId, int limit) {
        List<Review> reviews = reviewRepository.findReviewsByMember(authorId, cursorId, limit);
        return convertToResponse(reviews);
    }

    /**
     * 공통 메서드
     */
    private static List<ReviewResponse> convertToResponse(List<Review> reviews) {
        return reviews.stream()
                .map(it -> ReviewResponse.from(it, getOrderedMenusOfReview(it)))
                .toList();
    }

    // TODO: 주문 구현 후 리뷰의 실제 주문-장바구니-메뉴 리스트 가져오는 쪽으로 수정
    private static List<Menu> getOrderedMenusOfReview(Review review) {
        // 임시로 해당 가게의 모든 메뉴를 반환
        return review.getStore().getMenus();
    }

}
