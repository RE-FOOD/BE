package com.iitp.domains.member.dto.responseDto;

import lombok.Builder;

@Builder
public record MemberUpdateNicknameResponseDto(
        Long id,
        String nickname
) {
}
