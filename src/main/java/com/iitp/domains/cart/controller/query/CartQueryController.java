package com.iitp.domains.cart.controller.query;

import com.iitp.domains.cart.dto.request.CartCreateRequest;
import com.iitp.domains.cart.dto.response.CartResponse;
import com.iitp.domains.cart.service.command.CartCommandService;
import com.iitp.domains.cart.service.query.CartQueryService;
import com.iitp.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/carts")
@RestController
@Tag(name = "카트 Query API", description = "카트 Query API")
public class CartQueryController {
    private final CartQueryService cartQueryService;


    @Operation(summary = "카트 상세 페이지", description = "카트 상세 페이지 출력")
    @GetMapping("")
    public ApiResponse<CartResponse> findCart() {
        Long memberId = 1L;         // TODO :: User 연동되면 수정
        CartResponse response = cartQueryService.getCartFromRedis(memberId);
        return ApiResponse.ok(200,response,"카트 상세 페이지 출력");
    }
}