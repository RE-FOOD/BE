package com.iitp.domains.store.controller.command;

import com.iitp.domains.store.dto.request.MenuCreateRequest;
import com.iitp.domains.store.dto.request.MenuUpdateRequest;
import com.iitp.domains.store.service.command.MenuCommandService;
import com.iitp.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Tag(name = "메뉴 Command API", description = "메뉴 Command API")
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/menus")
@RestController
public class MenuCommandController {

    private final MenuCommandService menuCommandService;

    @Operation(summary = "메뉴 생성", description = "메뉴 생성합니다.")
    @PostMapping("")
    public ApiResponse<String> createMenu(@RequestBody MenuCreateRequest request, @PathVariable Long storeId) {

        menuCommandService.createMenu(request,storeId);

        return ApiResponse.ok(201, null,"메뉴 생성 성공");
    }

    @Operation(summary = "메뉴 수정", description = "메뉴 수정합니다.")
    @PatchMapping("/{menuId}")
    public ApiResponse<String> updateMenu(@RequestBody MenuUpdateRequest request, @PathVariable Long storeId, @PathVariable Long menuId) {
        menuCommandService.updateMenu(request,storeId,menuId);

        return ApiResponse.ok(200, null,"메뉴 수정 성공");
    }

    @Operation(summary = "메뉴 삭제", description = "메뉴를 삭제합니다.")
    @DeleteMapping("/{menuId}")
    public ApiResponse<String> deleteStore( @PathVariable Long storeId, @PathVariable Long menuId) {
        menuCommandService.deleteMenu(storeId,menuId);
        return ApiResponse.ok(200, null,"메뉴 삭제 성공");
    }
}
