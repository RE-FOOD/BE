package com.iitp.domains.review.domain.entity;

import static com.iitp.global.common.constants.BusinessLogicConstants.MAX_REVIEW_CONTENT_LENGTH;
import static com.iitp.global.common.constants.BusinessLogicConstants.MAX_REVIEW_RATING_VALUE;
import static com.iitp.global.common.constants.BusinessLogicConstants.MIN_REVIEW_RATING_VALUE;

import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.global.common.constants.BusinessLogicConstants;
import com.iitp.global.common.entity.BaseEntity;
import com.iitp.global.exception.BadRequestException;
import com.iitp.global.exception.ExceptionMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Review extends BaseEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "content", nullable = false)
    @Size(max = MAX_REVIEW_CONTENT_LENGTH)
    private String content;

    @Max(value = MAX_REVIEW_RATING_VALUE)
    @Min(value = MIN_REVIEW_RATING_VALUE)
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /**
     * 편의 메서드
     */
    public void update(String content, int rating) {
        this.content = content;

        if (!isRatingRangeValid(rating)) {
            throw new BadRequestException(ExceptionMessage.INVALID_REVIEW_RATING);
        }
        this.rating = rating;
    }

    // TODO:refactor: 어노테이션 검증 쓸지 말지 고민 (@Max 등)
    private boolean isRatingRangeValid(int rating) {
        return rating >= MIN_REVIEW_RATING_VALUE && rating <= MAX_REVIEW_RATING_VALUE;
    }

}
