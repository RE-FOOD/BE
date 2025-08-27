package com.iitp.domains.review.controller.query;

import com.iitp.domains.review.dto.response.MyReviewResponse;
import com.iitp.domains.review.dto.response.ReviewResponse;
import com.iitp.domains.review.service.query.ReviewQueryService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.common.response.TwoWayCursorListResponse;
import com.iitp.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/stores")
@RestController
@Tag(name = "리뷰 API", description = "리뷰 생성, 삭제, 조회 API")
public class ReviewQueryController {
    private final ReviewQueryService reviewQueryService;


    @GetMapping("/{storeId}/reviews")
    @Operation(summary = "특정 가게 리뷰 조회",
            description = "가게가 존재해야 합니다.")
    public ApiResponse<TwoWayCursorListResponse<ReviewResponse>> readStoreReviews(
            @PathVariable("storeId") long storeId,
            @RequestParam(value = "cursorId", defaultValue = "0") long cursorId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(reviewQueryService.readStoreReviews(storeId, cursorId, limit));
    }

    @GetMapping("/reviews/me")
    @Operation(summary = "내가 쓴 리뷰 조회",
            description = "회원이 존재해야 합니다.")
    public ApiResponse<TwoWayCursorListResponse<MyReviewResponse>> readStoreReviews(
            @RequestParam(value = "cursorId", defaultValue = "0") long cursorId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        return ApiResponse.ok(reviewQueryService.readMyReviews(memberId, cursorId, limit));
    }


}
