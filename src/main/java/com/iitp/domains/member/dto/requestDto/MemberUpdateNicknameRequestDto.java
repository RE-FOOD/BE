package com.iitp.domains.member.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record MemberUpdateNicknameRequestDto(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname
) {
}
