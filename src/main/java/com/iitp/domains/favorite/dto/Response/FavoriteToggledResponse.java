package com.iitp.domains.favorite.dto.Response;

import lombok.Builder;

@Builder
public record FavoriteToggledResponse(
        long storeId,
        boolean isFavored
) {
    public static FavoriteToggledResponse of(long storeId, boolean isFavored) {
        return FavoriteToggledResponse.builder()
                .storeId(storeId)
                .isFavored(isFavored)
                .build();
    }
}
