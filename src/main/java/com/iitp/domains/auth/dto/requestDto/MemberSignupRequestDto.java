package com.iitp.domains.auth.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record MemberSignupRequestDto(
        @NotBlank(message = "액세스 토큰은 필수입니다.")
        String accessToken,

        @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 전화번호 형식이 아닙니다.")
        String phone,

        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @NotBlank(message = "주소는 필수입니다.")
        String address
) {
}