package com.iitp.domains.payment.service;

import com.iitp.domains.cart.domain.entity.Cart;
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
import com.iitp.global.redis.service.CartRedisService;
import com.iitp.global.redis.service.CommonRedisService;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CommonRedisService commonRedisService;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;

    private static final String PENDING_ORDER_PREFIX = "pending_order:";
    private static final String PAYMENT_SESSION_PREFIX = "payment_session:";
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;

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
                throw new RuntimeException("만료되었거나 존재하지 않는 결제 세션입니다.");
            }

            // 2. 실제 Order 엔티티 생성 및 저장
            Order order = createOrderFromPendingOrder(pendingOrder);
            // TODO 여기서 에러
//            Order savedOrder = orderRepository.save(order);
//
//            // 3. 결제 정보 저장
//            Payment payment = createPaymentFromResponse(paymentResponse, savedOrder.getId());
//            paymentRepository.save(payment);
//
//            // 4. Redis에서 임시 데이터 삭제
//            commonRedisService.deleteValue(pendingOrderKey);

        } catch (Exception e) {
            throw new RuntimeException("주문 및 결제 정보 저장 실패", e);
        }
    }

    /**
     * PendingOrderDto로부터 Order 엔티티 생성
     */
    private Order createOrderFromPendingOrder(PendingOrderDto pendingOrder) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(pendingOrder.memberId())
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Store store = storeRepository.findByStoreId(pendingOrder.storeId())
                .orElseThrow(() -> new RuntimeException("가게를 찾을 수 없습니다."));

        // Cart는 기존에 저장되어 있으므로 조회
        // 여기서는 간단히 처리 (실제로는 CartRepository에서 조회 필요)

        return Order.builder()
                .member(member)
                .store(store)
                .cart(null) // Cart는 별도로 조회 필요
                .status(OrderStatus.PENDING)
                .pickupDueTime(pendingOrder.pickupDueTime())
                .totalAmount(pendingOrder.totalAmount())
                .isContainerReused(pendingOrder.isContainerReused())
                .build();
    }

    /**
     * 결제 응답으로부터 Payment 엔티티 생성
     */
    private Payment createPaymentFromResponse(JSONObject paymentResponse, Long orderId) {
        String paymentKey = (String) paymentResponse.get("paymentKey");
        String tossOrderId = (String) paymentResponse.get("orderId");
        String method = (String) paymentResponse.get("method");
        String status = (String) paymentResponse.get("status");
        Long totalAmount = (Long) paymentResponse.get("totalAmount");

        String approvedAtStr = (String) paymentResponse.get("approvedAt");
        LocalDateTime approvedAt = LocalDateTime.parse(approvedAtStr.replace("Z", ""));

        return Payment.builder()
                .orderId(orderId)
                .paymentKey(paymentKey)
                .tossOrderId(tossOrderId)
                .tossPaymentMethod(TossPaymentMethod.valueOf(method.toUpperCase()))
                .tossPaymentStatus(TossPaymentStatus.valueOf(status.toUpperCase()))
                .totalAmount(totalAmount)
                .requestedAt(LocalDateTime.now())
                .approvedAt(approvedAt)
                .build();
    }

    /**
     * Redis에서 PendingOrder 조회
     */
    public PendingOrderDto getPendingOrder(String sessionId) {
        String pendingOrderKey = PENDING_ORDER_PREFIX + sessionId;
        return commonRedisService.getValue(pendingOrderKey, PendingOrderDto.class);
    }
}