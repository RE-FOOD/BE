package com.iitp.domains.review.service.query;

import com.iitp.domains.cart.domain.entity.CartMenu;
import com.iitp.domains.cart.service.query.CartQueryService;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.domains.review.domain.entity.Review;
import com.iitp.domains.review.dto.response.MyReviewResponse;
import com.iitp.domains.review.dto.response.ReviewResponse;
import com.iitp.domains.review.repository.ReviewRepository;
import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.menu.MenuRepository;
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
    private final MenuRepository menuRepository;

    /**
     * API 응답 메서드
     */
    public TwoWayCursorListResponse<ReviewResponse> readStoreReviews(Long storeId, Long cursorId, int limit) {
        Store store = storeQueryService.findExistingStore(storeId);
        List<ReviewResponse> result = getStoreReviews(store.getId(), cursorId, limit);

        return new TwoWayCursorListResponse<>(
                result.isEmpty() ? null : result.getFirst().id(),
                result.isEmpty() ? null : result.getLast().id(),
                result
        );
    }

    public TwoWayCursorListResponse<MyReviewResponse> readMyReviews(long memberId, long cursorId, int limit) {
        Member author = memberQueryService.findMemberById(memberId);
        List<MyReviewResponse> result = getMyReviews(author.getId(), cursorId, limit);
        return new TwoWayCursorListResponse<>(
                result.isEmpty() ? null : result.getFirst().id(),
                result.isEmpty() ? null : result.getLast().id(),
                result
        );
    }

    /**
     * 조회 및 DTO 변환
     */
    public List<ReviewResponse> getStoreReviews(long storeId, Long cursorId, int limit) {
        List<Review> reviews = reviewRepository.findReviewsByStore(storeId, cursorId, limit);
        return convertToResponse(reviews);
    }

    private List<MyReviewResponse> getMyReviews(long authorId, Long cursorId, int limit) {
        List<Review> reviews = reviewRepository.findReviewsByMember(authorId, cursorId, limit);
        return convertToMyResponse(reviews);
    }

    /**
     * 공통 메서드
     */
    private List<ReviewResponse> convertToResponse(List<Review> reviews) {
        return reviews.stream()
                .map(it -> ReviewResponse.from(it, getOrderedMenusOfReview(it)))
                .toList();
    }

    private List<MyReviewResponse> convertToMyResponse(List<Review> reviews) {
        return reviews.stream()
                .map(it -> MyReviewResponse.from(it, getOrderedMenusOfReview(it)))
                .toList();
    }

    private List<Menu> getOrderedMenusOfReview(Review review) {
        // 임시로 해당 가게의 모든 메뉴를 반환
        System.out.println("review.getOrder().getId() = " + review.getOrder().getId());
        System.out.println("review.getOrder().getCart().getId() = " + review.getOrder().getCart().getId());

        List<Long> menuIdList = review.getOrder().getCart().getCartMenus().stream()
                .map(CartMenu::getMenuId)
                .toList();

        menuIdList.forEach(it-> System.out.println("it = " + it));

        return menuRepository.findAllById(menuIdList);
    }

}
