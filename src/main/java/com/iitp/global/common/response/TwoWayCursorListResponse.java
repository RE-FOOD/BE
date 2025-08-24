package com.iitp.global.common.response;

import java.util.List;
import lombok.Builder;

@Builder
public record TwoWayCursorListResponse<T>(
        Long prevCursor,
        Long nextCursor,
        List<T> list
) {
}
