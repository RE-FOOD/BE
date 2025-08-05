package com.iitp.domains.member.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record MemberLogInRequestDto(
        @NotBlank(message = "액세스 토큰은 필수입니다.")
        String accessToken
) {
}
