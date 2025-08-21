package com.iitp.domains.order.service.query;

import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.repository.OrderRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;

    public Order findExistingOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.ORDER_NOT_FOUND));
        if (order.getIsDeleted()) {
            throw new NotFoundException(ExceptionMessage.ORDER_NOT_FOUND);
        }
        return order;
    }
}
