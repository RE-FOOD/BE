package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.member.domain.EnvironmentLevel;
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
}
