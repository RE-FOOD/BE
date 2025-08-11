package com.iitp.domains.auth.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record FcmTokenUpdateRequestDto(
        @NotBlank(message = "FCM 토큰은 필수입니다.")
        @Size(max = 255, message = "FCM 토큰이 너무 깁니다.")
        String fcmToken
) {
}
