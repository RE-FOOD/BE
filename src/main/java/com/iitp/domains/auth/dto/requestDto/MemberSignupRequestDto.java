package com.iitp.domains.auth.dto.requestDto;

import com.iitp.domains.member.domain.entity.Member;
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

        String address,

        String roadAddress,

        Double latitude,

        Double longitude
) {

}