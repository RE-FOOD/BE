package com.iitp.domains.notification.dto;

public class FcmDto {
    public record FcmMessage(
            Boolean validate_only,
            Message message
    ) {}

    public record Message(
            String token,
            Notification notification
    ) {}

    public record Notification(
            String title,
            String body
    ) {}

    public record PushMessage(
            Long receiverId,
            String title,
            String body
    ) {}
}
