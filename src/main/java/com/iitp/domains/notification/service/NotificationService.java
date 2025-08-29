package com.iitp.domains.notification.service;

import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.service.query.MemberQueryService;

import com.iitp.domains.notification.domain.entity.Notification;
import com.iitp.domains.notification.dto.NotifyParams;
import com.iitp.domains.notification.repository.NotificationRepository;
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
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        receiver.addNotification(notification);
    }



}
