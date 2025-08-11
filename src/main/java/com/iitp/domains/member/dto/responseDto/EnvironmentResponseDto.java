package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.member.domain.EnvironmentLevel;
import com.iitp.domains.member.domain.entity.Member;
import lombok.Builder;

@Builder
public record EnvironmentResponseDto(
        EnvironmentLevel environmentLevel,
        Integer environmentPoint,
        Integer orderCount,
        Integer dishCount
) {
    /**
     * Member 엔티티로부터 생성
     */
    public static EnvironmentResponseDto from(Member member) {
        return EnvironmentResponseDto.builder()
                .environmentLevel(member.getEnvironmentLevel())
                .environmentPoint(member.getEnvironmentPoint())
                .orderCount(member.getOrderCount())
                .dishCount(member.getDishCount())
                .build();
    }
}
