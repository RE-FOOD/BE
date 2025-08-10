package com.iitp.domains.auth.dto.responseDto;

import com.iitp.domains.member.domain.JoinType;
import com.iitp.domains.member.domain.Role;
import com.iitp.domains.member.dto.responseDto.LocationResponseDto;
import lombok.Builder;

@Builder
public record MemberSignupResponseDto(
        Long id,
        String email,
        String nickname,
        String phone,
        Role role,
        JoinType joinType,
        Integer environmentLevel,
        String businessLicenseNumber,
        LocationResponseDto location,
        String accessToken,
        String refreshToken
) {
}
