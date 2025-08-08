package com.iitp.domains.store.controller.query;

import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.SortType;
import com.iitp.domains.store.dto.response.MenuListResponse;
import com.iitp.domains.store.dto.response.MenuResponse;
import com.iitp.domains.store.dto.response.StoreListResponse;
import com.iitp.domains.store.service.query.MenuQueryService;
import com.iitp.domains.store.service.query.StoreQueryService;
import com.iitp.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "가게 Query API", description = "가게 Query API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/menus")
public class MenuQueryController {
    private final MenuQueryService menuQueryService;

    @Operation(summary = "메뉴 상세 호출", description = "메뉴 상세 출력합니다.")
    @GetMapping("/{menuId}")
    public ApiResponse<MenuResponse> findMenu(@PathVariable Long storeId, @PathVariable Long menuId) {
        MenuResponse responses = menuQueryService.findMenu(storeId, menuId);
        return ApiResponse.ok(200,  responses, "상세 메뉴 호출 성공");
    }


    @Operation(summary = "가게 메뉴 리스트 호출", description = "가게 메뉴 리스트를 출력합니다.")
    @GetMapping("")
    public ApiResponse<List<MenuListResponse>> findMenus(@PathVariable Long storeId) {
        List<MenuListResponse> responses = menuQueryService.findMenus(storeId);
        return ApiResponse.ok(200,  responses, "메뉴 리스트 호출 성공");
    }
}
