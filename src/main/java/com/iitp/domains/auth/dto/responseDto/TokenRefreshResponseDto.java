package com.iitp.domains.auth.dto.responseDto;

public record TokenRefreshResponseDto(String accessToken, String refreshToken) {
}
