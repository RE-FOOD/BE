package com.iitp.domains.notification.dto;

import com.iitp.domains.notification.domain.entity.Notification;
import com.iitp.domains.notification.domain.entity.NotificationType;
import com.iitp.global.util.dateformat.CustomDateUtil;
import lombok.Builder;

public record NotificationResponse(
        Long id,
        NotificationType type,
        Long redirectTargetId,
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
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notificationType)
                .redirectTargetId(notification.getRedirectTargetId())
                .title(notification.getType().getTitle())
                .body(notification.getContent())
                .createdAt(CustomDateUtil.customDateFormat(notification.getCreatedAt(), "MM월 dd일 HH:MM"))
                .isRead(notification.getIsRead())
                .build();
    }

}