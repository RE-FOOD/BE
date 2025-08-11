package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.member.domain.EnvironmentLevel;
import com.iitp.domains.member.domain.entity.Member;
import lombok.Builder;

@Builder
public record MyPageResponseDto(
        Long id,
        String email,
        String nickname,
        EnvironmentLevel environmentLevel,
        Integer orderCount,           // 주문횟수
        Integer dishCount,           // 다회용기이용횟수
        Integer environmentScore    // 환경점수 (orderCount + dishCount)
) {
    /**
     * Member 엔티티로부터 생성
     */
    public static MyPageResponseDto from(Member member) {
        return MyPageResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .environmentLevel(member.getEnvironmentLevel())
                .orderCount(member.getOrderCount())
                .dishCount(member.getDishCount())
                .environmentScore(member.getEnvironmentPoint())
                .build();
    }
}
