package com.iitp.domains.store.controller.command;

import com.iitp.domains.store.dto.request.StoreCreateRequest;
import com.iitp.domains.store.dto.request.StoreUpdateRequest;
import com.iitp.domains.store.service.command.StoreCommandService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.config.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Tag(name = "가게 Command API", description = "가게 Command API")
@RequiredArgsConstructor
@RequestMapping("/api/stores")
@RestController
public class StoreCommandController {

    private final StoreCommandService storeCommandService;

    @Operation(summary = "가게 생성", description = "가게 정보를 입력 후 가게 생성합니다.")
    @PostMapping()
    public ApiResponse<Long> createStore(@RequestBody StoreCreateRequest request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Long storeId = storeCommandService.createStore(request,memberId);

        return ApiResponse.ok(201, storeId,"게시글 생성 성공");
    }

    @Operation(summary = "가게 수정", description = "가게 정보를 수정합니다.")
    @PatchMapping("/{storeId}")
    public ApiResponse<String> updateStore(@RequestBody StoreUpdateRequest request, @PathVariable Long storeId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        storeCommandService.updateStore(request,storeId, memberId);

        return ApiResponse.ok(200, null,"게시글 수정 성공");
    }

    @Operation(summary = "가게 삭제", description = "가게를 삭제합니다.")
    @DeleteMapping("/{storeId}")
    public ApiResponse<String> deleteStore( @PathVariable Long storeId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        storeCommandService.deleteStore(storeId, memberId);

        return ApiResponse.ok(200, null,"게시글 수정 성공");
    }
}
