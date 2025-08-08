package com.iitp.domains.store.dto.response;

import com.iitp.domains.store.domain.StoreStatus;
import com.iitp.domains.store.repository.mapper.StoreListQueryResult;

public record StoreListResponse(
        Long id,
        String name,
        StoreStatus status,
        String imageUrl,
        int discountPercent,                // 최고 할인율
        double ratingAvg,                   // 리뷰 평균 평점
        int count,                          // 리뷰 총 개수
        double distance                     // 내 위치부터 가게까지 거리
) {

    public static StoreListResponse fromQueryResult(StoreListQueryResult storeListQueryResult,String imageUrl, int percent, double rating, int count, double distance) {
        return new StoreListResponse(
                storeListQueryResult.id(),
                storeListQueryResult.name(),
                storeListQueryResult.status(),
                imageUrl,
                percent,
                rating,
                count,
                distance
        );
    }

}
