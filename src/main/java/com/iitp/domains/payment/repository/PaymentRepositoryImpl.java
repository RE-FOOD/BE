package com.iitp.domains.payment.repository;

import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.payment.domain.Payment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.iitp.domains.order.domain.entity.QOrder.order;
import static com.iitp.domains.payment.domain.QPayment.payment;

@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(payment)
                        .where(
                                payment.isDeleted.eq(false),
                                payment.orderId.eq(orderId)
                        )
                        .fetchOne()
        );
    }
}
