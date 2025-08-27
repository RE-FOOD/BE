package com.iitp.domains.store.dto.response;

import com.iitp.domains.store.domain.StoreStatus;
import com.iitp.domains.store.repository.mapper.StoreListQueryResult;
import lombok.Builder;

@Builder
public record FavoriteStoreItem(
        Long id,
        String name,
        StoreStatus status,
        String imageUrl,
        int discountPercent,
        Double ratingAvg,
        long count,
        double distance
) {
    public static FavoriteStoreItem fromQueryResult(
            StoreListQueryResult result,
            String imageUrl,
            double distance,
            StoreStatus actualStatus
    ) {
        return FavoriteStoreItem.builder()
                .id(result.id())
                .name(result.name())
                .status(actualStatus)
                .imageUrl(imageUrl)
                .discountPercent(result.maxPercent())
                .ratingAvg(result.ratingAvg())
                .count(result.count())
                .distance(distance)
                .build();
    }

    public static FavoriteStoreItem of(
            Long id, String name, StoreStatus status, String imageUrl,
            int discountPercent, Double ratingAvg, long count, double distance
    ) {
        return FavoriteStoreItem.builder()
                .id(id)
                .name(name)
                .status(status)
                .imageUrl(imageUrl)
                .discountPercent(discountPercent)
                .ratingAvg(ratingAvg)
                .count(count)
                .distance(distance)
                .build();
    }
}