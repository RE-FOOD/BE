package com.iitp.domains.review.repository;

import static com.iitp.domains.member.domain.entity.QMember.member;
import static com.iitp.domains.order.domain.entity.QOrder.order;
import static com.iitp.domains.review.domain.entity.QReview.review;

import com.iitp.domains.member.domain.entity.QMember;
import com.iitp.domains.order.domain.entity.QOrder;
import com.iitp.domains.review.domain.entity.Review;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Review> findReviewsByStore(long storeId, long cursorId, int limit) {
        // 존재하는 Store 조회 (isDeleted)
        List<Review> result = queryFactory.selectFrom(review)
                .join(review.order, order).fetchJoin()
                .join(review.member, member).fetchJoin()
                .where(review.store.isDeleted.isFalse())
                .where(review.store.id.eq(storeId))
                // 기본 정렬: 최신순
                .where(ltCursorId(cursorId))
                .orderBy(review.id.desc())
                // 개수 제한
                .limit(limit)
                .fetch();

        return result;
    }

    /**
     * Boolean Expression
     */

    private BooleanExpression ltCursorId(Long cursorId) {
        if (cursorId == null || cursorId == 0) return null;
        else return review.id.lt(cursorId);
    }

}
