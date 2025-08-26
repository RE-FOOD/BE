package com.iitp.domains.review.repository.mapper;

public record ReviewAggregationResult(
        Double averageRating,
        long count
) {
}
