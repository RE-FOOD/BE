package com.iitp.domains.order.service.command;

import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.dto.request.OrderCreateRequest;


public interface OrderCommandService {
    String createOrder(OrderCreateRequest request, Long memberId) ;

    Order orderSave(Order order);
}