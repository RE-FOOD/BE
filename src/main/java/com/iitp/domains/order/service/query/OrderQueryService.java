package com.iitp.domains.order.service.query;

import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.repository.OrderRepository;
import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import java.util.List;
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


    public List<Menu> findMenuList(Long orderId) {
        // Order -> Cart -> CartMenu -> Menu 리스트 조회
        // 1. 객체적으로 접근하기. 근데 지금은 CartMenu에서 Menu 엔티티 객체가 아닌 ID만을 FK로 갖고 있음.
        // 2. 쿼리를 통해 접근하기. QueryDSL을 써야 할듯? SELECT * FROM MENU WHERE

        return null;
    }
}
