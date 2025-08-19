package com.iitp.domains.cart.controller.command;

import com.iitp.domains.cart.dto.request.CartCreateRequest;
import com.iitp.domains.cart.dto.request.CartUpdateRequest;
import com.iitp.domains.cart.service.command.CartCommandService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.config.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/carts")
@RestController
@Tag(name = "장바구니 Command API", description = "장바구니 Command API")
public class CartCommandController {
    private final CartCommandService cartCommandService;


    @Operation(summary = "장바구니 메뉴 추가", description = "장바구니에 메뉴를 추가합니다.")
    @PostMapping("")
    public ApiResponse<String> addCart(@RequestBody CartCreateRequest request) {
        Long memberId = SecurityUtil.getCurrentMemberId(); // TODO :: User 연동되면 수정
        cartCommandService.addCart(memberId,request);
        return ApiResponse.ok(200,null,"장바구니 추가 성공");
    }


    @Operation(summary = "장바구니 메뉴 수량 변경", description = "장바구니 메뉴 수량을 변경합니다.")
    @PutMapping("")
    public ApiResponse<String> updateCart(@RequestBody CartUpdateRequest request) {
        Long memberId = SecurityUtil.getCurrentMemberId(); // TODO :: User 연동되면 수정
        cartCommandService.updateCart(memberId,request);

        return ApiResponse.ok(200,null,"장바구니 제품 수량 변경 성공");
    }
}
