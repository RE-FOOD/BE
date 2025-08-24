package com.iitp.domains.review.dto.request;


import static com.iitp.global.common.constants.BusinessLogicConstants.MAX_REVIEW_CONTENT_LENGTH;
import static com.iitp.global.common.constants.BusinessLogicConstants.MAX_REVIEW_RATING_VALUE;
import static com.iitp.global.common.constants.BusinessLogicConstants.MIN_REVIEW_RATING_VALUE;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
        @NotNull
        @Max(value = MAX_REVIEW_RATING_VALUE)
        @Min(value = MIN_REVIEW_RATING_VALUE)
        Integer rating,

        @NotNull
        @Size(max = MAX_REVIEW_CONTENT_LENGTH)
        String content
) {
}
