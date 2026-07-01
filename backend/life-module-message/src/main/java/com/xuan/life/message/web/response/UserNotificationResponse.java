package com.xuan.life.message.web.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

public record UserNotificationResponse(
    @JsonSerialize(using = ToStringSerializer.class)
    Long notificationId,
    String notificationType,
    @JsonSerialize(using = ToStringSerializer.class)
    Long senderUserId,
    String senderName,
    String senderAvatarUrl,
    String title,
    String contentText,
    boolean read,
    LocalDateTime createdAt,
    @JsonSerialize(using = ToStringSerializer.class)
    Long postId,
    @JsonSerialize(using = ToStringSerializer.class)
    Long commentId
) {
}
