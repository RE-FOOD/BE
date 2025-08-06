package com.iitp.domains.member.controller;

import com.iitp.domains.member.dto.requestDto.TokenRefreshRequestDto;
import com.iitp.domains.member.dto.responseDto.TokenRefreshResponseDto;
import com.iitp.domains.member.service.TokenService;
import com.iitp.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "토큰관리", description = "JWT 토큰 갱신 API")
public class TokenController {
    private final TokenService tokenService;

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponseDto>> refreshToken(
            @Valid @RequestBody TokenRefreshRequestDto request) {

        TokenRefreshResponseDto response = tokenService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(response, "토큰 갱신 완료"));
    }
}
