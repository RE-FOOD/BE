package com.iitp.domains.store.dto.response;

import java.util.List;

public record StoreListTotalResponse(
        Long prevCursor,
        Long nextCursor,
        boolean hasPrev,
        boolean hasNext,
        List<StoreListResponse> stores
) {
    // 빈 결과를 위한 정적 팩토리 메서드
    public static StoreListTotalResponse empty() {
        return new StoreListTotalResponse(null, null, false, false, List.of());
    }

    public static StoreListTotalResponse of(List<StoreListResponse> stores) {
        if (stores == null || stores.isEmpty()) {
            return empty();
        }

        Long prevCursor = stores.get(0).id();
        Long nextCursor = stores.get(stores.size() - 1).id();

        return new StoreListTotalResponse(prevCursor, nextCursor, true, true, stores);
    }

    public static StoreListTotalResponse ofWithPagination(
            List<StoreListResponse> stores,
            Long cursorId,
            boolean direction,
            int requestedLimit
    ) {
        if (stores == null || stores.isEmpty()) {
            return empty();
        }

        Long prevCursor = stores.get(0).id();
        Long nextCursor = stores.get(stores.size() - 1).id();

        boolean hasPrev;
        boolean hasNext;

        if (direction) {
            // 다음 페이지 요청인 경우
            hasPrev = cursorId != null && cursorId > 0; // 커서가 있으면 이전 페이지 존재
            hasNext = stores.size() == requestedLimit;   // 요청한 만큼 왔으면 다음 페이지 있을 가능성
        } else {
            // 이전 페이지 요청인 경우
            hasPrev = stores.size() == requestedLimit;   // 요청한 만큼 왔으면 이전 페이지 더 있을 가능성
            hasNext = true; // 이전 페이지를 요청했다는 것은 다음 페이지가 있다는 의미
        }

        return new StoreListTotalResponse(
                hasPrev ? prevCursor : null,
                hasNext ? nextCursor : null,
                hasPrev,
                hasNext,
                stores
        );
    }

    public static StoreListTotalResponse ofWithAccuratePagination(
            List<StoreListResponse> stores,
            boolean hasPrev,
            boolean hasNext
    ) {
        if (stores == null || stores.isEmpty()) {
            return empty();
        }

        Long prevCursor = hasPrev ? stores.get(0).id() : null;
        Long nextCursor = hasNext ? stores.get(stores.size() - 1).id() : null;

        return new StoreListTotalResponse(prevCursor, nextCursor, hasPrev, hasNext, stores);
    }
}