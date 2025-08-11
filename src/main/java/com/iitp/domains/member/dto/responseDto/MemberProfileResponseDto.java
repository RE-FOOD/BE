package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.member.domain.EnvironmentLevel;
import com.iitp.domains.member.domain.JoinType;
import com.iitp.domains.member.domain.Role;
import lombok.Builder;

@Builder
public record MemberProfileResponseDto(
        Long id,
        String email,
        String nickname,           // 개인회원만 (사업자는 null)
        String phone,
        Role role,
        JoinType joinType,
        EnvironmentLevel environmentLevel,
        Integer environmentPoint,
        Integer orderCount,
        Integer dishCount,
        String businessLicenseNumber,  // 사업자회원만 (개인회원은 null)
        Boolean isBusinessApproved,    // 사업자회원만 (개인회원은 null)
        LocationResponseDto location   // 개인회원만 (사업자는 null)
) {
    // 개인회원용
    public static MemberProfileResponseDto forUser(
            Long id, String email, String nickname, String phone,
            Role role, JoinType joinType, EnvironmentLevel environmentLevel,
            Integer environmentPoint, Integer orderCount, Integer dishCount,
            LocationResponseDto location) {

        return MemberProfileResponseDto.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .phone(phone)
                .role(role)
                .joinType(joinType)
                .environmentLevel(environmentLevel)
                .environmentPoint(environmentPoint)
                .orderCount(orderCount)
                .dishCount(dishCount)
                .businessLicenseNumber(null)
                .isBusinessApproved(null)
                .location(location)
                .build();
    }

    // 사업자회원용
    public static MemberProfileResponseDto forStore(
            Long id, String email, String phone, Role role, JoinType joinType,
            String businessLicenseNumber, Boolean isBusinessApproved) {

        return MemberProfileResponseDto.builder()
                .id(id)
                .email(email)
                .nickname(null)
                .phone(phone)
                .role(role)
                .joinType(joinType)
                .environmentLevel(null)
                .environmentPoint(null)
                .orderCount(null)
                .dishCount(null)
                .businessLicenseNumber(businessLicenseNumber)
                .isBusinessApproved(isBusinessApproved)
                .location(null)
                .build();
    }
}
