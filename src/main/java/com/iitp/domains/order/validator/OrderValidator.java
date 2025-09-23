package com.iitp.domains.order.validator;

import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.repository.OrderRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderValidator {
    private final OrderRepository orderRepository;

    public Order validateOrderExists(Long orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.ORDER_NOT_FOUND));
    }
}


