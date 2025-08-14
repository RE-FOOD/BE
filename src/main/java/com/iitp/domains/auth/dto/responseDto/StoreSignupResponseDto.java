package com.iitp.domains.auth.dto.responseDto;

import com.iitp.domains.member.domain.Role;
import com.iitp.domains.member.domain.entity.Member;
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
    /**
     * Member 엔티티로부터 생성
     */
    public static StoreSignupResponseDto from(Member member, String accessToken, String refreshToken) {
        return StoreSignupResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .phone(member.getPhone())
                .role(member.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .businessLicenseNumber(member.getBusinessLicenseNumber())
                .isBusinessApproved(member.getIsBusinessApproved() != null ?
                        member.getIsBusinessApproved().toString() : "미승인")
                .build();
    }
}
