package com.iitp.domains.payment.controller;


import com.iitp.domains.payment.dto.PendingOrderDto;
import com.iitp.domains.payment.dto.response.PaymentConfirmResponse;
import com.iitp.domains.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
@RequestMapping("/api/payments")
public class PaymentHtmlController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PaymentService paymentService;

    public PaymentHtmlController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 결제 페이지로 이동 (Redis에서 주문 정보 가져오기)
     */
    @GetMapping("/checkout/{sessionId}")
    public String checkout(@PathVariable String sessionId, Model model) {
        // Redis에서 임시 주문 정보 조회
        PendingOrderDto pendingOrder = paymentService.getPendingOrder(sessionId);

        if (pendingOrder == null) {
            // 세션이 만료되었거나 존재하지 않는 경우
            return "redirect:/error?message=결제세션이만료되었습니다";
        }

        // HTML로 전달할 데이터 설정
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("storeName", pendingOrder.storeName());
        model.addAttribute("totalAmount", pendingOrder.totalAmount());

        return "/checkout";
    }

    @RequestMapping(value = "/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody String jsonBody) throws Exception {
        JSONParser parser = new JSONParser();
        String orderId;
        String amount;
        String paymentKey;
        try {
            JSONObject requestData = (JSONObject) parser.parse(jsonBody);
            paymentKey = (String) requestData.get("paymentKey");
            orderId = (String) requestData.get("orderId");
            amount = (String) requestData.get("amount");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("paymentKey", paymentKey);

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

        // 결제 성공 시에만 주문과 결제 정보를 저장
        if (isSuccess) {
            try {
                // orderId는 실제로는 sessionId와 매핑되어야 함
                // 여기서는 간단히 처리
                String sessionId = orderId; // 실제로는 매핑 로직 필요
                PaymentConfirmResponse response = paymentService.saveOrderAndPayment(jsonObject, sessionId);
                System.out.println( "현재 유저 레벨 :" + response.level());
                System.out.println("현재 유저 레벨업 체크 " + response.levelCheck());

            } catch (Exception e) {
                logger.error("결제 성공 후 주문 저장 실패: {}", e.getMessage());
            }
        }


        return ResponseEntity.status(code).body(jsonObject);
    }

    /**
     * 인증성공처리
     */
    @RequestMapping(value = "/success", method = RequestMethod.GET)
    public String paymentRequest(HttpServletRequest request, Model model) throws Exception {
        logger.info("결제 성공!!!");
        return "/success";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(HttpServletRequest request, Model model) throws Exception {
        return "/checkout";
    }

    /**
     * 인증실패처리
     */
    @RequestMapping(value = "/fail", method = RequestMethod.GET)
    public String failPayment(HttpServletRequest request, Model model) throws Exception {
        logger.info("결제 실패!!!");
        String failCode = request.getParameter("code");
        String failMessage = request.getParameter("message");

        model.addAttribute("code", failCode);
        model.addAttribute("message", failMessage);

        return "/fail";
    }
}