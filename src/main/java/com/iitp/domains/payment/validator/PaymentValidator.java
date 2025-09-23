package com.iitp.domains.payment.validator;

import com.iitp.domains.payment.domain.Payment;
import com.iitp.domains.payment.repository.PaymentRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentValidator {
    private final PaymentRepository paymentRepository;

    public Payment validatePaymentExists(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.PAYMENT_NOT_FOUND));
    }
}
