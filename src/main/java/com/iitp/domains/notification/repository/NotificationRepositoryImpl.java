package com.iitp.domains.notification.repository;

import static com.iitp.domains.notification.domain.entity.QNotification.notification;

import com.iitp.domains.notification.domain.entity.Notification;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public List<Notification> findAllByMemberIdWithinOneMonth(Long memberId, Long cursorId, int limit){
        return queryFactory.selectFrom(notification)
                .where(notification.member.id.eq(memberId))
                .where(ltCursorId(cursorId))
                .where(notification.isDeleted.isFalse())
                .orderBy(notification.id.desc())
                .limit(limit)
                .fetch();
    };

    public List<Notification> findAllUnreadByMemberId(Long memberId) {
        return queryFactory.selectFrom(notification)
                .where(notification.member.id.eq(memberId))
                .where(notification.isRead.isFalse())
                .where(notification.isDeleted.isFalse())
                .fetch();
    }

    private static BooleanExpression ltCursorId(Long cursorId) {
        if (cursorId == null || cursorId == 0) {
            return null;
        } else {
            return notification.id.lt(cursorId);
        }
    }

}
