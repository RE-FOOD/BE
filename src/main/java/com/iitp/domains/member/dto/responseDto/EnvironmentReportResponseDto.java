package com.iitp.domains.member.dto.responseDto;

import com.iitp.global.common.constants.BusinessLogicConstants;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Builder
public record EnvironmentReportResponseDto(
        Integer orderCount,         // 전체 주문 횟수
        Integer dishCount,          // 전체 다회용기 사용 횟수
        Double totalTreesSaved,     // 절약된 나무 (소수점 첫째자리)
        Double totalCarbonSaved     // 절약된 탄소 (kg, 소수점 첫째자리)
) {
    /**
     * 전체 회원의 주문 횟수와 다회용기 사용 횟수로부터 환경 리포트 생성
     */
    public static EnvironmentReportResponseDto from(Integer totalOrderCount, Integer totalDishCount) {
        // 절약된 나무 계산 (소수점 첫째자리 반올림)
        double treesSaved = calculateTreesSaved(totalOrderCount, totalDishCount);

        // 절약된 탄소 계산 (소수점 첫째자리 반올림)
        double carbonSaved = calculateCarbonSaved(totalOrderCount, totalDishCount);

        return EnvironmentReportResponseDto.builder()
                .orderCount(totalOrderCount)
                .dishCount(totalDishCount)
                .totalTreesSaved(treesSaved)
                .totalCarbonSaved(carbonSaved)
                .build();
    }

    /**
     * 절약된 나무 계산 (소수점 첫째자리까지)
     */
    private static double calculateTreesSaved(int orderCount, int dishCount) {
        double orderTrees = orderCount * BusinessLogicConstants.SAVED_TREE_PER_ORDERED_MENU;
        double dishTrees = dishCount * BusinessLogicConstants.SAVED_TREE_PER_REUSING_CONTAINER;
        double totalTrees = orderTrees + dishTrees;

        return BigDecimal.valueOf(totalTrees)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 절약된 탄소 계산 (kg, 소수점 첫째자리까지)
     */
    private static double calculateCarbonSaved(int orderCount, int dishCount) {
        double orderCarbon = orderCount * BusinessLogicConstants.SAVED_CARBON_KG_PER_ORDERED_MENU;
        double dishCarbon = dishCount * BusinessLogicConstants.SAVED_CARBON_KG_PER_REUSING_CONTAINER;
        double totalCarbon = orderCarbon + dishCarbon;

        return BigDecimal.valueOf(totalCarbon)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
