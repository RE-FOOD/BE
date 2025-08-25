package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.member.domain.EnvironmentLevel;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.global.common.constants.BusinessLogicConstants;
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

        // 다음 레벨까지 필요한 점수와 진행률 계산
        LevelProgressInfo progressInfo = calculateLevelProgress(currentPoint, member.getEnvironmentLevel());

        return MyPageResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .environmentLevel(member.getEnvironmentLevel())
                .orderCount(member.getOrderCount())
                .dishCount(member.getDishCount())
                .environmentPoint(currentPoint)
                .nextLevelPoint(progressInfo.nextLevelPoint())
                .progressPercentage(progressInfo.progressPercentage())
                .build();
    }

    /**
     * 레벨 진행 정보를 계산하는 메서드
     */
    private static LevelProgressInfo calculateLevelProgress(int currentPoint, EnvironmentLevel currentLevel) {
        int currentLevelMinPoint = getCurrentLevelMinPoint(currentLevel);
        int nextLevelMinPoint = getNextLevelMinPoint(currentLevel);

        // 다음 레벨까지 필요한 점수
        int nextLevelPoint = Math.max(0, nextLevelMinPoint - currentPoint);

        // 현재 레벨에서의 진행률 계산
        double progressPercentage = calculateProgressPercentage(currentPoint, currentLevelMinPoint, nextLevelMinPoint);

        return new LevelProgressInfo(nextLevelPoint, progressPercentage);
    }

    /**
     * 현재 레벨의 최소 필요 점수를 반환
     */
    private static int getCurrentLevelMinPoint(EnvironmentLevel level) {
        return switch (level) {
            case SPROUT -> 0;   // 새싹: 0점부터
            case SEEDLING -> BusinessLogicConstants.ENVIRONMENT_LEVEL_ONE_REQUIRED_POINT;    // 묘목: 80점부터
            case TREE -> BusinessLogicConstants.ENVIRONMENT_LEVEL_TWO_REQUIRED_POINT;        // 나무: 160점부터
            case FRUIT -> BusinessLogicConstants.ENVIRONMENT_LEVEL_THREE_REQUIRED_POINT;     // 열매: 320점부터
        };
    }

    /**
     * 다음 레벨의 최소 필요 점수를 반환
     */
    private static int getNextLevelMinPoint(EnvironmentLevel level) {
        return switch (level) {
            case SPROUT -> BusinessLogicConstants.ENVIRONMENT_LEVEL_ONE_REQUIRED_POINT;      // 다음: 묘목 80점
            case SEEDLING -> BusinessLogicConstants.ENVIRONMENT_LEVEL_TWO_REQUIRED_POINT;    // 다음: 나무 160점
            case TREE -> BusinessLogicConstants.ENVIRONMENT_LEVEL_THREE_REQUIRED_POINT;      // 다음: 열매 320점
            case FRUIT -> BusinessLogicConstants.ENVIRONMENT_LEVEL_FOUR_REQUIRED_POINT;      // 최고레벨 320점 (더 이상 올라갈 레벨 없음)
        };
    }

    /**
     * 진행률을 계산 (소수점 둘째자리까지 반올림)
     */
    private static double calculateProgressPercentage(int currentPoint, int currentLevelMinPoint, int nextLevelMinPoint) {
        // 최고 레벨인 경우 100% 반환
        if (currentPoint >= BusinessLogicConstants.ENVIRONMENT_LEVEL_THREE_REQUIRED_POINT) {
            return 100.00;
        }

        // 현재 레벨에서 진행한 점수
        int progressPoint = currentPoint - currentLevelMinPoint;
        // 현재 레벨에서 다음 레벨까지 필요한 총 점수
        int totalPointForLevel = nextLevelMinPoint - currentLevelMinPoint;

        // 진행률 계산 (0~100%)
        double percentage = (double) progressPoint / totalPointForLevel * 100.0;

        // 소수점 둘째자리까지 반올림
        return BigDecimal.valueOf(percentage)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 레벨 진행 정보를 담는 내부 레코드
     */
    private record LevelProgressInfo(
            int nextLevelPoint,
            double progressPercentage
    ) {

    }
}
