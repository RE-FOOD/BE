package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.member.domain.JoinType;
import com.iitp.domains.member.domain.Role;
import lombok.Builder;

@Builder
public record MemberLogInResponseDto(
        Long id,

        String email,

        String nickname,

        String phone,

        Role role,

        JoinType joinType,

        Integer environmentLevel,

        LocationResponseDto location,

        String accessToken,

        String refreshToken
) {
}
