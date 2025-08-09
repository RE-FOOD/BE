package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.member.domain.EnvironmentLevel;
import lombok.Builder;

@Builder
public record EnvironmentResponseDto(
        EnvironmentLevel environmentLevel,
        Integer environmentPoint,
        Integer orderCount,
        Integer dishCount
) {
    // 환경데이터 생성
    public static EnvironmentResponseDto from(
            EnvironmentLevel environmentLevel,
            Integer environmentPoint,
            Integer orderCount,
            Integer dishCount) {

        return EnvironmentResponseDto.builder()
                .environmentLevel(environmentLevel)
                .environmentPoint(environmentPoint)
                .orderCount(orderCount)
                .dishCount(dishCount)
                .build();
    }
}
