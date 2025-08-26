package com.iitp.domains.auth.dto.responseDto;

import com.iitp.domains.member.domain.JoinType;
import com.iitp.domains.member.domain.Role;
import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.domain.entity.Member;
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
    /**
     * 개인 회원용
     */
    public static MemberSignupResponseDto forUser(
            Member member, Location location, String accessToken, String refreshToken) {

        return MemberSignupResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .role(member.getRole())
                .joinType(member.getJoinType())
                .environmentLevel(member.getEnvironmentLevel().getLevel())
                .businessLicenseNumber(null)
                .location(LocationResponseDto.from(location))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 사업자 회원용
     */
    public static MemberSignupResponseDto forStore(
            Member member, String accessToken, String refreshToken) {

        return MemberSignupResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(null)
                .phone(member.getPhone())
                .role(member.getRole())
                .joinType(member.getJoinType())
                .environmentLevel(null)
                .businessLicenseNumber(member.getBusinessLicenseNumber())
                .location(null)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
