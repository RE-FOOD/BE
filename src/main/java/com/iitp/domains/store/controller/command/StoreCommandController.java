package com.iitp.domains.store.controller.command;

import com.iitp.domains.store.dto.request.StoreCreateRequest;
import com.iitp.domains.store.service.command.StoreCommandService;
import com.iitp.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가게 Command API", description = "가게 Command API")
@RequiredArgsConstructor
@RequestMapping("/api/store")
@RestController
public class StoreCommandController {

    private final StoreCommandService storeCommandService;

    @Operation(summary = "가게 생성", description = "가게 정보를 입력 후 가게 생성합니다.")
    @PostMapping()
    public ApiResponse<String> createStore(@RequestBody StoreCreateRequest request) {
        Long userId = 1L;       // TODO :: 유저 연동되면 연결
        storeCommandService.createStore(request,userId);

        return ApiResponse.ok(201, null,"게시글 생성 성공");
    }
}
