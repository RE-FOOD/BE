package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.member.domain.EnvironmentLevel;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.global.util.environment.EnvironmentPointCalculator;
import com.iitp.global.util.environment.EnvironmentPointCalculator.LevelProgressInfo;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Builder
public record MyPageResponseDto(
        Long id,
        String email,
        String nickname,
        EnvironmentLevel environmentLevel,
        Integer orderCount,           // 주문횟수
        Integer dishCount,           // 다회용기이용횟수
        Integer environmentPoint,    // 환경점수 (orderCount + dishCount)
        Integer nextLevelPoint,      // 다음 레벨까지 필요한 점수
        Double progressPercentage    // 현재 레벨에서의 진행률 (소수점 둘째자리까지)
) {
    /**
     * Member 엔티티로부터 생성
     */
    public static MyPageResponseDto from(Member member) {
        // 현재 환경점수 계산
        int currentPoint = member.getEnvironmentPoint();
        EnvironmentLevel currentLevel = member.getEnvironmentLevel();

        // 레벨 진행 정보 계산
        LevelProgressInfo progressInfo = EnvironmentPointCalculator.calculateLevelProgress(currentPoint, currentLevel);

        return MyPageResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .environmentLevel(currentLevel)
                .orderCount(member.getOrderCount())
                .dishCount(member.getDishCount())
                .environmentPoint(currentPoint)
                .nextLevelPoint(progressInfo.nextLevelPoint())
                .progressPercentage(progressInfo.progressPercentage())
                .build();
    }
}
