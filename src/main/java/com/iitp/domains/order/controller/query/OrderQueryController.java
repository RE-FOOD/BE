package com.iitp.domains.order.controller.query;


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
@RequestMapping("/api/order")
public class OrderQueryController {
    private final OrderQueryService orderQueryService;

    @Operation(summary = "주문 조회", description = "주문 조회")
    @GetMapping("")
    public ApiResponse<OrderResponse> getOrder() throws Exception {
        Long memberId = SecurityUtil.getCurrentMemberId();
        OrderResponse response = orderQueryService.getOrder(memberId);

        return ApiResponse.ok(200,response,"주문 조회 성공");
    }
}
