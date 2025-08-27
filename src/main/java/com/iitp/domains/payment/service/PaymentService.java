package com.iitp.domains.payment.service;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.repository.CartRepository;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.repository.MemberRepository;
import com.iitp.domains.order.domain.OrderStatus;
import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.repository.OrderRepository;
import com.iitp.domains.payment.domain.Payment;
import com.iitp.domains.payment.domain.TossPaymentMethod;
import com.iitp.domains.payment.domain.TossPaymentStatus;
import com.iitp.domains.payment.dto.PaymentSessionDto;
import com.iitp.domains.payment.dto.PendingOrderDto;
import com.iitp.domains.payment.repository.PaymentRepository;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.exception.OrderConflictException;
import com.iitp.global.redis.service.CartRedisService;
import com.iitp.global.redis.service.CommonRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CommonRedisService commonRedisService;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final CartRepository cartRepository;
    private final CartRedisService cartRedisService;

    private static final String PENDING_ORDER_PREFIX = "pending_order:";
    private static final String PAYMENT_SESSION_PREFIX = "payment_session:";
    private static final String CART_CACHE_PREFIX = "cart:";
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;

    private final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    /**
     * 주문과 함께 결제를 진행하는 메서드
     */
    public String createPaymentSession(Order order, Cart cart) {
        try {
            // 1. 고유한 결제 세션 ID 생성
            String paymentSessionId = UUID.randomUUID().toString();

            // 2. PendingOrderDto 생성
            PendingOrderDto pendingOrder = PendingOrderDto.builder()
                    .sessionId(paymentSessionId)
                    .memberId(order.getMember().getId())
                    .storeId(order.getStore().getId())
                    .storeName(order.getStore().getName())
                    .cartId(cart.getId())
                    .totalAmount(order.getTotalAmount())
                    .pickupDueTime(order.getPickupDueTime())
                    .isContainerReused(order.getIsContainerReused())
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .build();

            // 3. Redis에 임시 주문 정보 저장 (30분 만료)
            String pendingOrderKey = PENDING_ORDER_PREFIX + paymentSessionId;
            commonRedisService.setValue(pendingOrderKey, pendingOrder, PAYMENT_TIMEOUT_MINUTES, TimeUnit.MINUTES);

            return paymentSessionId;

        } catch (Exception e) {
            throw new RuntimeException("결제 준비 실패", e);
        }
    }

    /**
     * 결제 성공 시 주문과 결제 정보를 저장
     */
    public void saveOrderAndPayment(JSONObject paymentResponse, String paymentSessionId) {
        try {
            // 1. Redis에서 임시 주문 정보 조회
            String pendingOrderKey = PENDING_ORDER_PREFIX + paymentSessionId;
            PendingOrderDto pendingOrder = commonRedisService.getValue(pendingOrderKey, PendingOrderDto.class);

            if (pendingOrder == null) {
                throw new NotFoundException(ExceptionMessage.ORDER_NOT_FOUND);
            }

            // 2. 실제 Order 엔티티 생성 및 저장
            Order order = createOrderFromPendingOrder(pendingOrder);

            Order savedOrder = orderRepository.save(order);

            // 3. 결제 정보 저장
            Payment payment = createPaymentFromResponse(paymentResponse, savedOrder.getId());
            paymentRepository.save(payment);

            // 4. Redis에서 임시 데이터 삭제
            // 결제 임시 데이터 삭제
            commonRedisService.deleteValue(pendingOrderKey);

            // 장바구니 데이터 삭제
            cartRedisService.deleteCart(CART_CACHE_PREFIX + pendingOrder.memberId());

        } catch (Exception e) {
            throw new RuntimeException("주문 및 결제 정보 저장 실패", e);
        }
    }

    /**
     * PendingOrderDto로부터 Order 엔티티 생성
     */
    private Order createOrderFromPendingOrder(PendingOrderDto pendingOrder) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(pendingOrder.memberId())
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));

        Store store = storeRepository.findByStoreId(pendingOrder.storeId())
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.STORE_NOT_FOUND));

        Cart cart = cartRepository.findByCartId(pendingOrder.cartId())
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.CART_NOT_FOUND));


        return Order.builder()
                .member(member)
                .store(store)
                .cart(cart) // Cart는 별도로 조회 필요
                .status(OrderStatus.COMPLETED)
                .pickupDueTime(pendingOrder.pickupDueTime())
                .totalAmount(pendingOrder.totalAmount())
                .isContainerReused(pendingOrder.isContainerReused())
                .build();
    }

    private Payment createPaymentFromResponse(JSONObject paymentResponse, Long orderId) {
        try {
            // null 체크 및 안전한 파싱
            String paymentKey = getStringValue(paymentResponse, "paymentKey");
            String tossOrderId = getStringValue(paymentResponse, "orderId");
            String method = getStringValue(paymentResponse, "method");
            String status = getStringValue(paymentResponse, "status");

            // totalAmount는 JSON에서 String으로 올 수 있으므로 안전하게 파싱
            Long totalAmount = getLongValue(paymentResponse, "totalAmount");

            // approvedAt 처리
            LocalDateTime approvedAt = null;
            String approvedAtStr = getStringValue(paymentResponse, "approvedAt");
            if (approvedAtStr != null && !approvedAtStr.trim().isEmpty()) {
                try {
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse(approvedAtStr);
                    approvedAt = offsetDateTime.toLocalDateTime();
                } catch (Exception e) {
                    logger.warn("approvedAt 파싱 실패: {}, 현재 시간 사용", approvedAtStr, e);
                    approvedAt = LocalDateTime.now();
                }
            } else {
                approvedAt = LocalDateTime.now();
            }

            // enum 변환 시 안전하게 처리
            TossPaymentMethod paymentMethod = parsePaymentMethod(method);
            TossPaymentStatus paymentStatus = parsePaymentStatus(status);

            return Payment.builder()
                    .orderId(orderId)
                    .paymentKey(paymentKey)
                    .tossOrderId(tossOrderId)
                    .tossPaymentMethod(paymentMethod)
                    .tossPaymentStatus(paymentStatus)
                    .totalAmount(totalAmount)
                    .requestedAt(LocalDateTime.now())
                    .approvedAt(approvedAt)
                    .build();

        } catch (Exception e) {
            logger.error("Payment 엔티티 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("결제 정보 파싱 실패", e);
        }
    }

    // 안전한 String 값 추출
    private String getStringValue(JSONObject json, String key) {
        Object value = json.get(key);
        return value != null ? value.toString() : null;
    }

    // 안전한 Long 값 추출
    private Long getLongValue(JSONObject json, String key) {
        Object value = json.get(key);
        if (value == null) return null;

        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                logger.warn("totalAmount 파싱 실패: {}", value);
                return 0L;
            }
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    // 안전한 PaymentMethod 파싱
    private TossPaymentMethod parsePaymentMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            return TossPaymentMethod.CARD; // 기본값
        }

        try {
            return TossPaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("알 수 없는 결제 방법: {}, 기본값 사용", method);
            return TossPaymentMethod.CARD;
        }
    }

    // 안전한 PaymentStatus 파싱
    private TossPaymentStatus parsePaymentStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return TossPaymentStatus.DONE; // 기본값
        }

        try {
            return TossPaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("알 수 없는 결제 상태: {}, 기본값 사용", status);
            return TossPaymentStatus.DONE;
        }
    }

    /**
     * Redis에서 PendingOrder 조회
     */
    public PendingOrderDto getPendingOrder(String sessionId) {
        String pendingOrderKey = PENDING_ORDER_PREFIX + sessionId;
        return commonRedisService.getValue(pendingOrderKey, PendingOrderDto.class);
    }
}