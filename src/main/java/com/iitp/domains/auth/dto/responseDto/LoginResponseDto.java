package com.iitp.domains.auth.dto.responseDto;

import lombok.Builder;

@Builder
public record LoginResponseDto(
        String accessToken,
        String refreshToken,
        String fcmToken
) {
    public static LoginResponseDto of(
            String accessToken, String refreshToken, String fcmToken) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .fcmToken(fcmToken)
                .build();
    }
}
