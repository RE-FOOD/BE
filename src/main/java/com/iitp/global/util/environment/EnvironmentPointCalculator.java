package com.iitp.global.util.environment;

import com.iitp.domains.member.domain.EnvironmentLevel;
import com.iitp.global.common.constants.BusinessLogicConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EnvironmentPointCalculator {

    /**
     * 환경 포인트에 따른 레벨 계산
     */
    public static EnvironmentLevel calculateEnvironmentLevel(int points) {
        if (points >= BusinessLogicConstants.ENVIRONMENT_LEVEL_THREE_REQUIRED_POINT) {
            return EnvironmentLevel.FRUIT;    // 5600점 이상 - 열매
        } else if (points >= BusinessLogicConstants.ENVIRONMENT_LEVEL_TWO_REQUIRED_POINT) {
            return EnvironmentLevel.TREE;     // 2400점 이상 - 나무
        } else if (points >= BusinessLogicConstants.ENVIRONMENT_LEVEL_ONE_REQUIRED_POINT) {
            return EnvironmentLevel.SEEDLING; // 800점 이상 - 묘목
        } else {
            return EnvironmentLevel.SPROUT;   // 800점 미만 - 새싹
        }
    }

    /**
     * 현재 레벨의 최소 필요 점수를 반환
     */
    public static int getCurrentLevelMinPoint(EnvironmentLevel level) {
        return switch (level) {
            case SPROUT -> 0;   // 새싹: 0점부터
            case SEEDLING -> BusinessLogicConstants.ENVIRONMENT_LEVEL_ONE_REQUIRED_POINT;    // 묘목: 800점부터
            case TREE -> BusinessLogicConstants.ENVIRONMENT_LEVEL_TWO_REQUIRED_POINT;        // 나무: 2400점부터
            case FRUIT -> BusinessLogicConstants.ENVIRONMENT_LEVEL_THREE_REQUIRED_POINT;     // 열매: 5600점부터
        };
    }

    /**
     * 다음 레벨의 최소 필요 점수를 반환
     */
    public static int getNextLevelMinPoint(EnvironmentLevel level) {
        return switch (level) {
            case SPROUT -> BusinessLogicConstants.ENVIRONMENT_LEVEL_ONE_REQUIRED_POINT;      // 다음: 묘목 800점
            case SEEDLING -> BusinessLogicConstants.ENVIRONMENT_LEVEL_TWO_REQUIRED_POINT;    // 다음: 나무 2400점
            case TREE -> BusinessLogicConstants.ENVIRONMENT_LEVEL_THREE_REQUIRED_POINT;      // 다음: 열매 5600점
            case FRUIT -> BusinessLogicConstants.ENVIRONMENT_LEVEL_FOUR_REQUIRED_POINT;      // 최고레벨 5600점
        };
    }

    /**
     * 다음 레벨까지 필요한 점수 계산
     */
    public static int calculateNextLevelPoint(int currentPoint, EnvironmentLevel currentLevel) {
        int nextLevelMinPoint = getNextLevelMinPoint(currentLevel);
        return Math.max(0, nextLevelMinPoint - currentPoint);
    }

    /**
     * 현재 레벨에서의 진행률 계산 (소수점 둘째자리까지 반올림)
     */
    public static double calculateProgressPercentage(int currentPoint, EnvironmentLevel currentLevel) {
        // 최고 레벨인 경우 100% 반환
        if (currentLevel == EnvironmentLevel.FRUIT) {
            return 100.00;
        }

        int currentLevelMinPoint = getCurrentLevelMinPoint(currentLevel);
        int nextLevelMinPoint = getNextLevelMinPoint(currentLevel);

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
     * 레벨 진행 정보를 한번에 계산
     */
    public static LevelProgressInfo calculateLevelProgress(int currentPoint, EnvironmentLevel currentLevel) {
        int nextLevelPoint = calculateNextLevelPoint(currentPoint, currentLevel);
        double progressPercentage = calculateTotalProgressPercentage(currentPoint);

        return new LevelProgressInfo(nextLevelPoint, progressPercentage);
    }

    /**
     * 주문 금액에 따른 환경 포인트 계산
     *
     * @param orderAmount 주문 금액
     * @return 계산된 환경 포인트 (250원당 1점, 최대 200점)
     */
    public static int calculateOrderEnvironmentPoint(int orderAmount) {
        // 250원당 1포인트 계산
        int calculatedPoint = orderAmount / BusinessLogicConstants.PRICE_UNIT_FOR_POINT;

        // 최대 포인트 제한 (200점)
        return Math.min(calculatedPoint, BusinessLogicConstants.ENVIRONMENT_MAX_POINT_PER_ORDER);
    }
    /**
     * 다회용기 사용에 따른 환경 포인트 반환
     *
     * @return 다회용기 사용 포인트 (50점)
     */
    public static int calculateContainerReuseEnvironmentPoint() {
        return BusinessLogicConstants.ENVIRONMENT_POINT_PER_REUSING_CONTAINER;
    }

    /**
     * 전체 포인트 대비 현재 포인트의 퍼센트 계산 (소수점 둘째자리까지 반올림)
     * 최대 포인트는 5600점 (FRUIT 레벨 최소 요구 점수)
     */
    public static double calculateTotalProgressPercentage(int currentPoint) {
        // 최대 포인트 (FRUIT 레벨 최소 요구 점수)
        int maxPoint = BusinessLogicConstants.ENVIRONMENT_LEVEL_THREE_REQUIRED_POINT;

        // 전체 대비 진행률 계산 (0~100%)
        double percentage = (double) currentPoint / maxPoint * 100.0;

        // 100%를 넘지 않도록 제한
        percentage = Math.min(percentage, 100.0);

        // 소수점 둘째자리까지 반올림
        return BigDecimal.valueOf(percentage)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 주문 완료시 총 환경 포인트 계산
     *
     * @param orderAmount 주문 금액
     * @param isContainerReused 다회용기 사용 여부
     * @return 총 환경 포인트
     */
    public static int calculateTotalEnvironmentPoint(int orderAmount, boolean isContainerReused) {
        int orderPoint = calculateOrderEnvironmentPoint(orderAmount);
        int containerPoint = isContainerReused ? calculateContainerReuseEnvironmentPoint() : 0;

        return orderPoint + containerPoint;
    }

    /**
     * 레벨 진행 정보를 담는 레코드
     */
    public record LevelProgressInfo(
            int nextLevelPoint,
            double progressPercentage
    ) {}
}
