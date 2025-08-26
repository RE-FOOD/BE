package com.iitp.domains.review.service.command;

import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.domains.order.domain.OrderStatus;
import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.repository.OrderRepository;
import com.iitp.domains.order.service.query.OrderQueryService;
import com.iitp.domains.review.domain.entity.Review;
import com.iitp.domains.review.dto.request.ReviewCreateRequest;
import com.iitp.domains.review.repository.ReviewRepository;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.service.query.StoreQueryService;
import com.iitp.global.exception.BadRequestException;
import com.iitp.global.exception.ConflictException;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReviewCommandService {
    private final ReviewRepository reviewRepository;
    // TODO: 주문 구현 완료 후 orderRepository 의존관계 제거
    private final OrderRepository orderRepository;
    private final MemberQueryService memberQueryService;
    private final StoreQueryService storeQueryService;
    private final OrderQueryService orderQueryService;


    public void writeReview(Long memberId, Long storeId, Long orderId, ReviewCreateRequest request) {
        Member member = memberQueryService.findMemberById(memberId);    // 리팩토링 고민: 회원 조회 쿼리
        Store store = storeQueryService.findExistingStore(storeId);
        // TODO: 주문 구현 완료 후 연결 및 아래 구문 삭제
        Order order = Order.builder()
                .member(member)
                .store(store)
                .status(OrderStatus.COMPLETED)
                .totalAmount(10000)
                .pickupDueTime(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        orderRepository.save(order);
        orderRepository.flush();
//        Order order = orderQueryService.findExistingOrder(orderId);

        // 완료된 주문인지 여부 검증
        if (!order.isCompleted()) {
            throw new BadRequestException(ExceptionMessage.ORDER_STATUS_NOT_COMPLETED);
        }

        // 해당 주문에 대해 이미 리뷰가 있는지 여부 검증
        Boolean isPresent = reviewRepository.existsByMemberIdAndOrderId(memberId, orderId);
        if (isPresent) {
            throw new ConflictException(ExceptionMessage.ALREADY_EXISTING_REVIEW_OF_ORDER);
        }

        member.addReview(Review.builder()
                .member(member)
                .store(store)
                .order(order)
                .content(request.content())
                .rating(request.rating())
                .build()
        );
    }

    public void deleteReview(Long memberId, Long storeId, Long orderId, Long reviewId) {
        // 각 데이터에 대해 존재 여부 검증
        Member member = memberQueryService.findMemberById(memberId);    // 리팩토링 고민: 회원 조회 쿼리
        storeQueryService.findExistingStore(storeId);
        orderQueryService.findExistingOrder(orderId);
        Review review = findExistingReview(reviewId);   // 리팩토링 고민: 조회할 때 storeId, orderId를 모두 써야 하나. 일치 여부 검증?

        // 분기: 작성자인지 여부 검증
        validateIsAuthor(member, review);

        review.markAsDeleted();
    }

    private static void validateIsAuthor(Member member, Review review) {
        boolean isPresent = member.getReviews().stream().anyMatch(it -> it.equals(review));
        if (!isPresent) {
            throw new BadRequestException(ExceptionMessage.ACCESS_DENIED_NOT_AUTHOR);
        }
    }

    public Review findExistingReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.REVIEW_NOT_FOUND));
        if (review.getIsDeleted()) {
            throw new NotFoundException(ExceptionMessage.REVIEW_NOT_FOUND);
        }
        return review;
    }

}
