package com.iitp.domains.map.dto.requestDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
@Schema(description = "지도 기반 가게 검색 요청")
public record MapStoreSearchRequestDto(
        @Schema(description = "사용자 위치 - 위도", example = "37.5665")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
        @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
        double latitude,

        @Schema(description = "사용자 위치 - 경도", example = "126.9780")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
        @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
        double longitude,

        @Schema(description = "검색 반경 (km)", example = "3.0", defaultValue = "5.0")
        @DecimalMin(value = "0.1", message = "검색 반경은 0.1km 이상이어야 합니다")
        @DecimalMax(value = "50.0", message = "검색 반경은 50km 이하여야 합니다")
        Double radiusKm,

        @Schema(description = "최대 결과 수", example = "20", defaultValue = "30")
        @Min(value = 1, message = "최대 결과 수는 1 이상이어야 합니다")
        @Max(value = 100, message = "최대 결과 수는 100 이하여야 합니다")
        Integer limit
) {
        // 정적 생성 메서드
        public static MapStoreSearchRequestDto of(double latitude, double longitude, Double radiusKm, Integer limit) {
                return MapStoreSearchRequestDto.builder()
                        .latitude(latitude)
                        .longitude(longitude)
                        .radiusKm(radiusKm)
                        .limit(limit)
                        .build();
        }
        public static MapStoreSearchRequestDto of(double latitude, double longitude) {
                return of(latitude, longitude, null, null);
        }

        // 기본값 제공 메서드
        public double getRadiusKm() {
                return radiusKm != null ? radiusKm : 5.0;
        }

        public int getLimit() {
                return limit != null ? limit : 30;
        }

        // 유효성 검증 메서드
        public boolean isValidCoordinate() {
                return latitude >= -90.0 && latitude <= 90.0 &&
                        longitude >= -180.0 && longitude <= 180.0;
        }
}
