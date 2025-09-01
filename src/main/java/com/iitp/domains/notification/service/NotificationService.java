package com.iitp.domains.notification.service;

import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.service.query.MemberQueryService;

import com.iitp.domains.notification.domain.entity.Notification;
import com.iitp.domains.notification.dto.NotificationResponse;
import com.iitp.domains.notification.dto.NotifyParams;
import com.iitp.domains.notification.repository.NotificationRepository;
import com.iitp.global.common.response.TwoWayCursorListResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final FcmService fcmService;
    private final NotificationRepository notificationRepository;
    private final MemberQueryService memberQueryService;

    @Transactional
    public void pushMessage(NotifyParams params) {
        Member receiver = memberQueryService.findMemberById(params.receiver().getId());

        fcmService.sendPushMessage(receiver.getFcmToken(), params);
        Notification notification = Notification.builder()
                .member(receiver)
                .content(params.content())
                .type(params.type())
                .redirectTargetId(params.redirectTargetId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        receiver.addNotification(notification);
    }

    @Transactional
    public TwoWayCursorListResponse<NotificationResponse> findMyNotifications(Long memberId, Long cursorId, int limit) {
        // 알림 리스트 조회
        List<NotificationResponse> result = notificationRepository
                .findAllByMemberIdWithinOneMonth(memberId, cursorId, limit)
                .stream().map(NotificationResponse::of).toList();

        // 조회 대상과 별개로 전체 일괄 읽음 처리
        readAllNotifications(memberId);

        // TwoWayCursorListResponse cursor Id 처리
        return new TwoWayCursorListResponse<>(
                result.isEmpty() ? null : result.getFirst().id(),
                result.isEmpty() ? null : result.getLast().id(),
                result
        );
    }

    private void readAllNotifications(Long memberId) {
        List<Notification> unreadNotifications = notificationRepository.findAllUnreadByMemberId(memberId);
        unreadNotifications.forEach(Notification::read);
    }

}
