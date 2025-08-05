package com.iitp.domains.store.controller.command;

import com.iitp.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가게 CUD API", description = "가게 CUD API")
@RequiredArgsConstructor
@RequestMapping("/api/store")
@RestController
public class StoreController {

    @Operation(summary = "가게 생성", description = "가게 정보를 입력 후 가게 생성합니다.")
    @PostMapping()
    public void createStore() {
    }
}
