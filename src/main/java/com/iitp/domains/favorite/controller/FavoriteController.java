package com.iitp.domains.favorite.controller;

import com.iitp.domains.favorite.dto.Response.FavoriteToggledResponse;
import com.iitp.domains.favorite.service.command.FavoriteCommandService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.config.security.MemberPrincipal;
import com.iitp.global.config.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Tag(name = "찜", description = "가게에 대한 찜 API")
public class FavoriteController {
    private final FavoriteCommandService favoriteCommandService;

    @PatchMapping("/{storeId}/favorites")
    @Operation(summary = "찜 토글", description = "기존 찜 존재 여부에 따라 특정 가게에 대한 찜을 생성 혹은 삭제합니다.")
    public ApiResponse<FavoriteToggledResponse> toggleFavorite(@PathVariable("storeId") Long storeId, @AuthenticationPrincipal
    MemberPrincipal memberPrincipal) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        FavoriteToggledResponse favoriteToggledResponse = favoriteCommandService.toggleFavorite(storeId);
        return ApiResponse.ok(200, favoriteToggledResponse, "찜 토글 완료");
    }
}

