package com.iitp.domains.cart.controller.query;

import com.iitp.domains.cart.dto.request.CartCreateRequest;
import com.iitp.domains.cart.dto.response.CartResponse;
import com.iitp.domains.cart.service.command.CartCommandService;
import com.iitp.domains.cart.service.query.CartQueryService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.config.security.SecurityUtil;
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


    @Operation(summary = "기존 가게 장바구니 확인", description = "기존 동일 가게 장바구니 존재 확인")
    @GetMapping("/check")
    public ApiResponse<String> checkDuplicate(@RequestParam Long storeId) {
        Long memberId = SecurityUtil.getCurrentMemberId(); // TODO :: User 연동되면 수정
        String[] response = cartQueryService.getCartDuplicate(storeId, memberId);
        return ApiResponse.ok(Integer.valueOf(response[0]),null,response[1]);
    }


    @Operation(summary = "카트 상세 페이지", description = "카트 상세 페이지 출력")
    @GetMapping("")
    public ApiResponse<CartResponse> findCart() {
        Long memberId = SecurityUtil.getCurrentMemberId(); // TODO :: User 연동되면 수정
        CartResponse response = cartQueryService.getCartFromRedis(memberId);
        return ApiResponse.ok(200,response,"장바구니 정보 가져오기 성공");
    }
}