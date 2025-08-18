package com.iitp.domains.map.dto.responseDto;

import com.iitp.domains.store.domain.StoreStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "지도 기반 가게 검색 응답")
public record MapStoreResponseDto(
        @Schema(description = "가게 ID", example = "1")
        Long storeId,

        @Schema(description = "가게 이름", example = "맛있는 식당")
        String storeName,

        @Schema(description = "가게 영업 상태")
        StoreStatus status,

        @Schema(description = "가게 위치 - 위도", example = "37.5665")
        double latitude,

        @Schema(description = "가게 위치 - 경도", example = "126.9780")
        double longitude,

        @Schema(description = "사용자 위치로부터의 거리 (km)", example = "1.2")
        double distanceKm,

        @Schema(description = "가게 대표 이미지 URL")
        String imageUrl,

        @Schema(description = "최대 할인율 (%)", example = "20")
        int maxDiscountPercent,

        @Schema(description = "리뷰 평균 평점", example = "4.5")
        double reviewRating,

        @Schema(description = "리뷰 총 개수", example = "127")
        int reviewCount,

        @Schema(description = "찜 여부", example = "true")
        boolean isFavorited
) {

    /**
     * 모든 파라미터를 받는 정적 생성 메서드
     */
    public static MapStoreResponseDto of(
            Long storeId,
            String storeName,
            StoreStatus status,
            double latitude,
            double longitude,
            double distanceKm,
            String imageUrl,
            int maxDiscountPercent,
            double reviewRating,
            int reviewCount,
            boolean isFavorited
    ) {
        return MapStoreResponseDto.builder()
                .storeId(storeId)
                .storeName(storeName)
                .status(status)
                .latitude(latitude)
                .longitude(longitude)
                .distanceKm(roundToTwoDecimal(distanceKm))
                .imageUrl(imageUrl)
                .maxDiscountPercent(maxDiscountPercent)
                .reviewRating(roundToOneDecimal(reviewRating))
                .reviewCount(reviewCount)
                .isFavorited(isFavorited)
                .build();
    }

    /**
     * MapStoreQueryResult와 추가 정보로 생성하는 정적 메서드
     */
    public static MapStoreResponseDto from(
            MapStoreQueryResult storeResult,
            double distanceKm,
            String imageUrl,
            int maxDiscountPercent,
            double reviewRating,
            int reviewCount,
            boolean isFavorited
    ) {
        return of(
                storeResult.storeId(),
                storeResult.storeName(),
                storeResult.status(),
                storeResult.latitude(),
                storeResult.longitude(),
                distanceKm,
                imageUrl,
                maxDiscountPercent,
                reviewRating,
                reviewCount,
                isFavorited
        );
    }

    // 소수점 반올림 유틸리티 메서드
    private static double roundToTwoDecimal(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
