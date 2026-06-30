package com.xuan.life.message.web.response;

public record NotificationStreamEventResponse(
    String eventType,
    long unreadCount,
    UserNotificationResponse notification
) {
}
