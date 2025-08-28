package com.iitp.domains.order.controller.query;


import com.iitp.domains.order.dto.response.OrderListResponse;
import com.iitp.domains.order.dto.response.OrderPaymentResponse;
import com.iitp.domains.order.dto.response.OrderResponse;
import com.iitp.domains.order.service.query.OrderQueryService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.config.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "주문 Query API", description = "주문 Query API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderQueryController {
    private final OrderQueryService orderQueryService;

    @Operation(summary = "주문 조회", description = "주문 조회")
    @GetMapping("")
    public ApiResponse<OrderResponse> getOrderReady() throws Exception {
        Long memberId = SecurityUtil.getCurrentMemberId();
        OrderResponse response = orderQueryService.getOrderReady(memberId);

        return ApiResponse.ok(200,response,"주문 조회 성공");
    }

    @Operation(summary = "주문 내역 리스트 조회", description = "주문 내역 리스트 조회")
    @GetMapping("/list")
    public ApiResponse<OrderListResponse> getOrders(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "cursorId", defaultValue = "0") Long cursorId
    ) throws Exception {
        Long memberId = SecurityUtil.getCurrentMemberId();
        OrderListResponse response = orderQueryService.getOrders(keyword, cursorId, memberId);

        return ApiResponse.ok(200,response,"주문 조회 성공");
    }

    @Operation(summary = "주문 내역 상세 조회", description = "주문 내역 상세 조회")
    @GetMapping("/{orderId}")
    public ApiResponse<OrderPaymentResponse> getOrders(
        @PathVariable(name = "orderId") Long orderId
    ) throws Exception {
        Long memberId = SecurityUtil.getCurrentMemberId();
        OrderPaymentResponse response = orderQueryService.getOrder(orderId, memberId);

        return ApiResponse.ok(200,response,"주문 내역 상세 조회");
    }
}
