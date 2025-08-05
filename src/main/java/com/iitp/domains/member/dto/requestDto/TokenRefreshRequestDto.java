package com.iitp.domains.member.dto.requestDto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequestDto(
        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        String refreshToken
) {
}
