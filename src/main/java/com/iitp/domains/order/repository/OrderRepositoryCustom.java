package com.iitp.domains.order.repository;

import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.dto.response.OrderPaymentResponse;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryCustom {
    List<Order> findOrders(String keyword, Long cursorId, Long memberId);

    Optional<Order> findByOrderId(Long orderId);

    // 새로운 메서드 추가
    OrderPaymentResponse findOrderWithDetails(Long orderId, Long memberId);
}
