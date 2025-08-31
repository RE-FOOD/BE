package com.iitp.domains.order.repository;

import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.dto.response.OrderPaymentResponse;
import com.iitp.domains.store.dto.response.StoreOrderListResponse;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryCustom {
    List<Order> findOrders(String keyword, Long cursorId, Long memberId);

    Optional<Order> findByOrderId(Long orderId);

    // 새로운 메서드 추가
    OrderPaymentResponse findOrderWithDetails(Long orderId, Long memberId);

    List<Order> findByStoreId(Long storeId, Long cursorId);

    // Store ID로 주문과 메뉴 정보를 함께 조회
    List<StoreOrderListResponse> findOrdersWithMenuInfo(Long storeId, Long cursorId);
}
