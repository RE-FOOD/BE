package com.iitp.domains.map.dto.responseDto;

import lombok.Builder;

import java.util.List;

@Builder
public record MapListScrollResponseDto(
        List<MapListResponseDto> stores,
        Long cursorId,
        Long prevCursor,
        Long nextCursor,
        Boolean hasNext
) {
    public static MapListScrollResponseDto of(List<MapListResponseDto> stores, Long cursorId,
                                              Long prevCursor, Long nextCursor, Boolean hasNext) {
        return MapListScrollResponseDto.builder()
                .stores(stores)
                .cursorId(cursorId)
                .prevCursor(prevCursor)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    public static MapListScrollResponseDto empty(Long cursorId) {
        return MapListScrollResponseDto.builder()
                .stores(List.of())
                .cursorId(cursorId)
                .prevCursor(null)
                .nextCursor(null)
                .hasNext(false)
                .build();
    }
    public static MapListScrollResponseDto empty() {
        return empty(null);
    }
}
