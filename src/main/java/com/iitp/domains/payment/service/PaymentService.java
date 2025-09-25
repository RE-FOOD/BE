package com.iitp.domains.payment.service;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.payment.dto.PendingOrderDto;
import com.iitp.domains.payment.dto.response.PaymentConfirmResponse;
import org.json.simple.JSONObject;


public interface PaymentService {

    /**
     * 주문과 함께 결제를 진행하는 메서드
     */
    String createPaymentSession(Order order, Cart cart);

    /**
     * 결제 성공 시 주문과 결제 정보를 저장
     */
    PaymentConfirmResponse saveOrderAndPayment(JSONObject paymentResponse, String paymentSessionId);


    /**
     * Redis에서 PendingOrder 조회
     */
    PendingOrderDto getPendingOrder(String sessionId);
}