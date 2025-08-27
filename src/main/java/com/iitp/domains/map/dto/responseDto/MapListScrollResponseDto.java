package com.iitp.domains.map.dto.responseDto;

import lombok.Builder;

import java.util.List;

@Builder
public record MapListScrollResponseDto(
        List<MapListResponseDto> stores,
        Long prevCursor,
        Long nextCursor,
        Boolean hasNext
) {
    public static MapListScrollResponseDto of(List<MapListResponseDto> stores,
                                              Long prevCursor, Long nextCursor, Boolean hasNext) {
        return MapListScrollResponseDto.builder()
                .stores(stores)
                .prevCursor(prevCursor)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    public static MapListScrollResponseDto empty() {
        return MapListScrollResponseDto.builder()
                .stores(List.of())
                .prevCursor(null)
                .nextCursor(null)
                .hasNext(false)
                .build();
    }
}
