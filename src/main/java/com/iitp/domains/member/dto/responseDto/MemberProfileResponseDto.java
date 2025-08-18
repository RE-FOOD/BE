package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.member.domain.BusinessApprovalStatus;
import com.iitp.domains.member.domain.EnvironmentLevel;
import com.iitp.domains.member.domain.JoinType;
import com.iitp.domains.member.domain.Role;
import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.domain.entity.Member;
import lombok.Builder;

@Builder
public record MemberProfileResponseDto(
        Long id,
        String email,
        String nickname,           // 개인회원만 (사업자는 null)
        String phone,
        Role role,
        JoinType joinType,
        String businessLicenseNumber,  // 사업자회원만 (개인회원은 null)
        BusinessApprovalStatus isBusinessApproved,    // 사업자회원만 (개인회원은 null)
        String location   // 개인회원만 (사업자는 null)
) {
    /**
     * 개인회원용
     */
    public static MemberProfileResponseDto forUser(Member member, Location location) {
        return MemberProfileResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .role(member.getRole())
                .joinType(member.getJoinType())
                .businessLicenseNumber(null)
                .isBusinessApproved(null)
                .location(location.getAddress())
                .build();
    }
    /**
     * 사업자회원용
     */
    public static MemberProfileResponseDto forStore(Member member) {
        return MemberProfileResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(null)
                .phone(member.getPhone())
                .role(member.getRole())
                .joinType(member.getJoinType())
                .businessLicenseNumber(member.getBusinessLicenseNumber())
                .isBusinessApproved(member.getIsBusinessApproved())
                .location(null)
                .build();
    }
}
