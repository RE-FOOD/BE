package com.iitp.domains.notification.repository;

import com.iitp.domains.notification.domain.entity.Notification;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepositoryCustom {
    List<Notification> findAllByMemberIdWithinOneMonth(Long memberId, Long cursorId, int limit);
    List<Notification> findAllUnreadByMemberId(Long memberId);
}
