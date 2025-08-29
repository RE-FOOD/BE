package com.iitp.domains.notification.dto;

import com.iitp.domains.notification.domain.entity.Notification;
import com.iitp.domains.notification.domain.entity.NotificationType;
import com.iitp.global.util.dateformat.CustomDateUtil;
import lombok.Builder;

public record NotificationResponse(
        Long notificationId,
        NotificationType notificationType,
        String title,
        String body,
        String createdAt,
        boolean isRead
) {
    @Builder
    public NotificationResponse {
    }

    public static NotificationResponse of(Notification notification) {
        NotificationType notificationType = notification.getType();
        String targetClassName = notificationType.getRedirectTargetClass().getSimpleName();
        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .notificationType(notificationType)
                .title(notification.getType().getTitle())
                .body(notification.getContent())
                .createdAt(CustomDateUtil.customDateFormat(notification.getCreatedAt(), "MM월 DD일 HH:MM"))
                .isRead(notification.getIsRead())
                .build();
    }

}