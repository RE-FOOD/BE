package com.iitp.domains.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessApprovalStatus {
    PENDING("대기"),    // 승인 대기
    APPROVED("승인"),   // 승인됨
    REJECTED("거절");   // 거절됨
    private final String description;
}
