package com.iitp.domains.store.dto.response;

import com.iitp.domains.store.domain.StoreStatus;
import com.iitp.domains.store.repository.mapper.StoreListQueryResult;

public record StoreListResponse(
        Long id,
        String name,
        StoreStatus status,
        String imageUrl,
        int discountPercent,                // 최고 할인율
        Double ratingAvg,                   // 리뷰 평균 평점
        long count,                         // 리뷰 총 개수
        double distance                     // 내 위치부터 가게까지 거리
) {

    // 정적 메서드: QueryResult에서 Response로 변환
    public static StoreListResponse fromQueryResult(
            StoreListQueryResult result,
            String imageUrl,
            double distance,
            StoreStatus actualStatus
    ) {
        return new StoreListResponse(
                result.id(),
                result.name(),
                actualStatus,  // 실제 영업 상태
                imageUrl,
                result.maxPercent(),
                result.ratingAvg(),
                result.count(),
                distance
        );
    }

    // 정적 메서드: 모든 파라미터 직접 지정
    public static StoreListResponse of(
            Long id, String name, StoreStatus status, String imageUrl,
            int discountPercent, Double ratingAvg, long count, double distance
    ) {
        return new StoreListResponse(id, name, status, imageUrl,
                discountPercent, ratingAvg, count, distance);
    }
}