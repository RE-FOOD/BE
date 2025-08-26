package com.iitp.global.common.response;

import java.util.List;

// 리팩토링 고민: TwoWayCursorListResponse 레코드 -> 클래스로 바꾸고 상속
public record CursorPaginationStoreResponse<T>(
        Long prevCursor,
        Long nextCursor,
        Double prevDistance,
        Double nextDistance,
        List<T> list
) {
}
