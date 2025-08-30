package com.iitp.domains.payment.controller;


import com.iitp.domains.payment.dto.PendingOrderDto;
import com.iitp.domains.payment.dto.request.PaymentRequest;
import com.iitp.domains.payment.dto.response.PaymentConfirmResponse;
import com.iitp.domains.payment.dto.response.PaymentFailResponse;
import com.iitp.domains.payment.dto.response.PaymentResponse;
import com.iitp.domains.payment.service.PaymentService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PaymentService paymentService;

    /**
     * 결제 페이지로 이동 (Redis에서 주문 정보 가져오기)
     */
    @GetMapping("/checkout/{sessionId}")
    public ApiResponse<PaymentResponse> checkout(@PathVariable String sessionId) {
        // Redis에서 임시 주문 정보 조회
        PendingOrderDto pendingOrder = paymentService.getPendingOrder(sessionId);

        if (pendingOrder == null) {
            // 세션이 만료되었거나 존재하지 않는 경우
            throw new NotFoundException(ExceptionMessage.SESSION_EXPIRED);
        }

        PaymentResponse response = new PaymentResponse(sessionId, pendingOrder.storeName(), pendingOrder.totalAmount());

        return ApiResponse.ok(200, response,"결제 정보 캐시 조회 성공");
    }

    @PostMapping(value = "/confirm")
    public ApiResponse<PaymentConfirmResponse> confirmPayment(@RequestBody PaymentRequest request) throws Exception {
        JSONParser parser = new JSONParser();

        JSONObject obj = new JSONObject();
        obj.put("orderId", request.orderId());
        obj.put("amount", request.amount());
        obj.put("paymentKey", request.paymentKey());

        String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";

        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(obj.toString().getBytes("UTF-8"));

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200;

        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        responseStream.close();

        PaymentConfirmResponse response = null;

        // 결제 성공 시에만 주문과 결제 정보를 저장
        if (isSuccess) {
            try {
                // orderId는 실제로는 sessionId와 매핑되어야 함
                // 여기서는 간단히 처리
                String sessionId = request.orderId(); // 실제로는 매핑 로직 필요
                response  = paymentService.saveOrderAndPayment(jsonObject, sessionId);
            } catch (Exception e) {
                logger.error("결제 성공 후 주문 저장 실패: {}", e.getMessage());
            }
        }
        return ApiResponse.ok(200, response,"결제 성공");
    }


    /**
     * 인증실패처리
     */
    @GetMapping("/fail")
    public ApiResponse<PaymentFailResponse> failPayment(HttpServletRequest request) throws Exception {

        String failCode = request.getParameter("code");
        String failMessage = request.getParameter("message");

        PaymentFailResponse response = new PaymentFailResponse(failCode, failMessage);

        return ApiResponse.ok(200, response,"");
    }
}