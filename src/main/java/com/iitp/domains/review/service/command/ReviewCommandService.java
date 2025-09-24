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
import com.iitp.domains.store.validator.StoreValidator;
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


public interface ReviewCommandService {

    void writeReview(Long memberId, Long storeId, Long orderId, ReviewCreateRequest request);

    void deleteReview(Long memberId, Long storeId, Long orderId, Long reviewId);

    Review findExistingReview(Long reviewId);

}
