package com.iitp.domains.auth.dto.responseDto;

import com.iitp.domains.member.domain.BusinessApprovalStatus;
import com.iitp.domains.member.domain.Role;
import com.iitp.domains.member.domain.entity.Member;
import lombok.Builder;

@Builder
public record LoginResponseDto(
        String accessToken,
        String refreshToken,
        String fcmToken,
        Long memberId,
        String email,
        Role role,
        BusinessApprovalStatus businessApprovalStatus // 사업자 승인 상태 (일반회원은 null)
) {
    /**
     * Member 객체를 받아서 LoginResponseDto 생성
     */
    public static LoginResponseDto from(Member member, String accessToken, String refreshToken) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .fcmToken(member.getFcmToken())
                .memberId(member.getId())
                .email(member.getEmail())
                .role(member.getRole())
                .businessApprovalStatus(member.getIsBusinessApproved())
                .build();
    }
}
