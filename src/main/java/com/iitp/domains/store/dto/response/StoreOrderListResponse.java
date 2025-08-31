package com.iitp.domains.store.dto.response;

import com.iitp.domains.order.domain.OrderStatus;
import com.iitp.domains.store.domain.entity.Menu;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.List;

@Builder
public record StoreOrderListResponse (
    long orderId,
    Timestamp pickupDueTime,
    List<String> menus,
    int menuCount,
    int totalAmount,
    OrderStatus status
){
}
