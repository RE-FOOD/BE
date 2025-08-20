package com.iitp.domains.store.dto.response;

import java.util.List;

public record StoreListTotalResponse(
        Long prevCursor,                // 마지막 ID 값
        Long nextCursor,                 // 첫번째 Id
        List<StoreListResponse> stores
) {
}
