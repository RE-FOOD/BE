package com.iitp.domains.review.controller.command;

import com.iitp.domains.review.dto.request.ReviewCreateRequest;
import com.iitp.domains.review.service.command.ReviewCommandService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}")
@RestController
@Tag(name = "리뷰 API", description = "리뷰 생성, 삭제, 조회 API")
public class ReviewCommandController {
    private final ReviewCommandService reviewCommandService;

    @PostMapping("/orders/{orderId}/reviews")
    @Operation(
            summary = "리뷰 생성",
            description = "별점(1~5)과 내용(2000자)이 유효해야 합니다. 가게 및 주문이 존재해야 합니다. 주문은 완료 상태여야 합니다."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<String> createReview(
            @PathVariable Long storeId,
            @PathVariable Long orderId,
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        System.out.println(memberId);
        reviewCommandService.writeReview(memberId, storeId, orderId, request);

        return ApiResponse.created();
    }

}
