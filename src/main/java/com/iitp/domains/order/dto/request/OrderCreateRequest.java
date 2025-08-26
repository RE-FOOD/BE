package com.iitp.domains.order.dto.request;


import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.order.domain.OrderStatus;
import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.store.domain.entity.Store;

import java.sql.Timestamp;


public record OrderCreateRequest (
        Timestamp pickupDueAt,
        boolean reuse
){

    public static Order toEntity(Member member, Store store, Cart cart, Timestamp pickupDueAt, boolean reuse) {
        return Order.builder()
                .member(member)
                .store(store)
                .cart(cart)
                .status(OrderStatus.PENDING)
                .pickupDueTime(pickupDueAt)
                .totalAmount(cart.getTotalPrice())
                .isContainerReused(reuse)
                .build();
    }
}
