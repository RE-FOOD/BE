package com.iitp.domains.store.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FavoriteStoresResponse(
        Long prevCursor,
        Long nextCursor,
        boolean hasPrev,
        boolean hasNext,
        List<FavoriteStoreItem> stores
) {
    // 빈 결과를 위한 정적 메서드
    public static FavoriteStoresResponse empty() {
        return FavoriteStoresResponse.builder()
                .prevCursor(null)
                .nextCursor(null)
                .hasPrev(false)
                .hasNext(false)
                .stores(List.of())
                .build();
    }

    // 단방향 무한 스크롤용 정적 메서드
    public static FavoriteStoresResponse of(List<FavoriteStoreItem> stores, Long cursorId, boolean hasNext) {
        if (stores == null || stores.isEmpty()) {
            return empty();
        }

        Long prevCursor = (cursorId != null && cursorId > 0) ? stores.get(0).id() : null;
        Long nextCursor = hasNext ? stores.get(stores.size() - 1).id() : null;
        boolean hasPrev = cursorId != null && cursorId > 0;

        return FavoriteStoresResponse.builder()
                .prevCursor(prevCursor)
                .nextCursor(nextCursor)
                .hasPrev(hasPrev)
                .hasNext(hasNext)
                .stores(stores)
                .build();
    }
}