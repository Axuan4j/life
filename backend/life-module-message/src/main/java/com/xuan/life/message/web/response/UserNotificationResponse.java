package com.xuan.life.message.web.response;

import java.time.LocalDateTime;

public record UserNotificationResponse(
    Long notificationId,
    String notificationType,
    Long senderUserId,
    String senderName,
    String senderAvatarUrl,
    String title,
    String contentText,
    boolean read,
    LocalDateTime createdAt,
    Long postId,
    Long commentId
) {
}
