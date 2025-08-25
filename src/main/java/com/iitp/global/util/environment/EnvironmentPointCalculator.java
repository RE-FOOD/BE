package com.iitp.global.util.environment;

import com.iitp.global.common.constants.BusinessLogicConstants;

public class EnvironmentPointCalculator {
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
}
