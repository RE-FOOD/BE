package com.iitp.domains.store.domain;

public enum SortType {
    NEAR("가까운 순"),
    REVIEW("리뷰 많은 순"),
    RATING("평점 높은 순");

    private final String description;

    SortType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
