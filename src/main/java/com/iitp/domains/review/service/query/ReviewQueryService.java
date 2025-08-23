package com.iitp.domains.review.service.query;

import com.iitp.domains.cart.service.query.CartQueryService;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.domains.order.repository.OrderRepository;
import com.iitp.domains.order.service.query.OrderQueryService;
import com.iitp.domains.review.domain.entity.Review;
import com.iitp.domains.review.dto.response.ReviewResponse;
import com.iitp.domains.review.repository.ReviewRepository;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.service.query.StoreQueryService;
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
    // TODO: 주문 구현 완료 후 orderRepository 의존관계 제거
    private final OrderRepository orderRepository;
    private final MemberQueryService memberQueryService;
    private final StoreQueryService storeQueryService;
    private final OrderQueryService orderQueryService;
    private final CartQueryService cartQueryService;

    public List<ReviewResponse> readStoreReviews(
            Long memberId,
            Long storeId,
            Long cursorId,
            int limit
    ) {
        Store store = storeQueryService.findExistingStore(storeId);

        // 조회
        List<Review> reviews = reviewRepository.findReviewsByStore(store.getId(), cursorId, limit);
        reviews.forEach(it -> System.out.println(it.getContent()));

        // TODO: 주문 구현 후 실제 리뷰의 주문-장바구니-메뉴 리스트 가져오는 쪽으로 수정
        List<ReviewResponse> result = reviews.stream()
                .map(it -> ReviewResponse.from(it, store.getMenus()))
                .toList();
        return result;
    }

    // 내가 쓴 리뷰 목록 조회


}
