package com.iitp.domains.payment.repository;

import com.iitp.domains.payment.domain.Payment;

import java.util.Optional;

public interface PaymentRepositoryCustom {
    Optional<Payment> findByOrderId(Long orderId);
}
