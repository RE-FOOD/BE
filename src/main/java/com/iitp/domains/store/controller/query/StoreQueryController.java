package com.iitp.domains.store.controller.query;

import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.SortType;
import com.iitp.domains.store.dto.response.FavoriteStoresResponse;
import com.iitp.domains.store.dto.response.StoreDetailResponse;
import com.iitp.domains.store.dto.response.StoreListTotalResponse;
import com.iitp.domains.store.dto.response.StoreOrderListResponse;
import com.iitp.domains.store.service.query.StoreQueryService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.config.security.CustomUserDetails;
import com.iitp.global.config.security.SecurityUtil;
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

@Tag(name = "가게 Query API", description = "가게 Query API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreQueryController {
    private final StoreQueryService storeQueryService;

    @Operation(summary = "가게 리스트 호출", description = "필터에 적합한 가게 리스트를 출력합니다.")
    @GetMapping("")
    public ApiResponse<StoreListTotalResponse> findStores(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "category", required = false) Category category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", required = false) SortType sort,
            @RequestParam(value = "cursorId", defaultValue = "0") Long cursorId,
            @RequestParam(value = "direction", defaultValue = "true") boolean direction,
            @RequestParam(value = "limit", defaultValue = "15") int limit
    ) {
        Long memberId = userDetails.getMemberId();
        StoreListTotalResponse responses = storeQueryService
                .findStores(memberId, category, keyword, sort, cursorId, direction, limit);

        return ApiResponse.ok(200, responses, "가게 리스트 호출 성공");
    }


    @Operation(summary = "가게 세부 내용 호출", description = "가게 세부 내용 출력합니다.")
    @GetMapping("/{storeId}")
    public ApiResponse<StoreDetailResponse> findStore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId
    ) {
        Long memberId = userDetails.getMemberId();
        StoreDetailResponse responses = storeQueryService.findStoreData(memberId, storeId);

        return ApiResponse.ok(200, responses, "상세 가게 호출 성공");
    }

    @Operation(summary = "찜한 가게 목록 조회", description = "사용자가 찜한 가게 목록을 조회합니다. (단방향 무한 스크롤)")
    @GetMapping("/favorites/me")
    public ApiResponse<FavoriteStoresResponse> getFavoriteStores(
            @RequestParam(value = "sort", required = false, defaultValue = "REVIEW") SortType sort,
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            @RequestParam(value = "limit", defaultValue = "15") int limit
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // Service 호출
        FavoriteStoresResponse response = storeQueryService.findMyFavoriteStores(
                memberId, sort, cursorId, limit
        );

        return ApiResponse.ok(200, response, "찜한 게시글 목록 호출 성공");
    }


    @Operation(summary = "가게 주문 리스트 호출", description = "가게 주문 리스트를 출력합니다.")
    @GetMapping("/orders")
    public ApiResponse<List<StoreOrderListResponse>> findOrderFromStores(
            @RequestParam(value = "cursorId", defaultValue = "0") Long cursorId
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        List<StoreOrderListResponse> responses = storeQueryService
                .findOrders(memberId, cursorId);

        return ApiResponse.ok(200, responses, "가게 주문 리스트 호출 성공");
    }


}
