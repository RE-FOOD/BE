package com.iitp.domains.order.controller.command;


import com.iitp.domains.order.dto.request.OrderCreateRequest;
import com.iitp.domains.order.service.command.OrderCommandService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.config.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Tag(name = "주문 Command API", description = "주문 Command API")
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@RestController
@Transactional
public class OrderCommandController {

    private final OrderCommandService orderCommandService;

    @Operation(summary = "주문 생성", description = "주문과 결제 진행")
    @PostMapping("")
    public ApiResponse<String> confirmPayment(@RequestBody OrderCreateRequest request) throws Exception {
        Long memberId = SecurityUtil.getCurrentMemberId();
        String paymentSessionId = orderCommandService.createOrder(request,memberId);

        return ApiResponse.ok(200,paymentSessionId,"주문 성공, 결제 진행");
    }


}