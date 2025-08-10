package com.iitp.domains.auth.dto.responseDto;

import com.iitp.domains.member.domain.Role;
import lombok.Builder;

@Builder
public record StoreSignupResponseDto(
        Long id,
        String email,
        String phone,
        Role role,
        String accessToken,
        String refreshToken,
        String businessLicenseNumber,
        String isBusinessApproved
) {
}
