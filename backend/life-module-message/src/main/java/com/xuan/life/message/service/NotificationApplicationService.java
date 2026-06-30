package com.xuan.life.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.life.common.api.PageResponse;
import com.xuan.life.message.entity.UserNotification;
import com.xuan.life.message.mapper.UserNotificationMapper;
import com.xuan.life.message.model.NotificationEventType;
import com.xuan.life.message.model.NotificationType;
import com.xuan.life.message.web.response.NotificationStreamEventResponse;
import com.xuan.life.message.web.response.UnreadCountResponse;
import com.xuan.life.message.web.response.UserNotificationResponse;
import com.xuan.life.user.entity.UserAccount;
import com.xuan.life.user.entity.UserProfile;
import com.xuan.life.user.mapper.UserAccountMapper;
import com.xuan.life.user.mapper.UserProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationApplicationService {

    private static final int DEFAULT_PREVIEW_LENGTH = 72;

    private final UserNotificationMapper userNotificationMapper;
    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;
    private final NotificationStreamHub notificationStreamHub;

    public NotificationApplicationService(
        UserNotificationMapper userNotificationMapper,
        UserAccountMapper userAccountMapper,
        UserProfileMapper userProfileMapper,
        NotificationStreamHub notificationStreamHub
    ) {
        this.userNotificationMapper = userNotificationMapper;
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
        this.notificationStreamHub = notificationStreamHub;
    }

    @Transactional
    public void createLikeNotification(
        Long actorUserId,
        Long receiverUserId,
        Long postId,
        String sourceContentText
    ) {
        if (actorUserId == null || receiverUserId == null || Objects.equals(actorUserId, receiverUserId)) {
            return;
        }
        String actorName = senderName(actorUserId);
        UserNotification notification = buildNotification(
            receiverUserId,
            NotificationType.LIKE,
            actorUserId,
            actorName,
            actorName + " 赞了你的动态",
            previewText(sourceContentText),
            postId,
            null
        );
        persistAndPushAfterCommit(notification);
    }

    @Transactional
    public void createRepostNotification(
        Long actorUserId,
        Long receiverUserId,
        Long postId,
        String repostContentText
    ) {
        if (actorUserId == null || receiverUserId == null || Objects.equals(actorUserId, receiverUserId)) {
            return;
        }
        String actorName = senderName(actorUserId);
        UserNotification notification = buildNotification(
            receiverUserId,
            NotificationType.REPOST,
            actorUserId,
            actorName,
            actorName + " 转发了你的动态",
            previewText(repostContentText),
            postId,
            null
        );
        persistAndPushAfterCommit(notification);
    }

    @Transactional
    public void createCommentNotification(
        Long actorUserId,
        Long postAuthorUserId,
        Long replyToUserId,
        Long postId,
        Long commentId,
        String commentContentText
    ) {
        if (actorUserId == null || postAuthorUserId == null || postId == null) {
            return;
        }

        String actorName = senderName(actorUserId);
        List<UserNotification> pendingNotifications = new ArrayList<>();

        if (!Objects.equals(actorUserId, postAuthorUserId)) {
            pendingNotifications.add(buildNotification(
                postAuthorUserId,
                NotificationType.COMMENT,
                actorUserId,
                actorName,
                actorName + " 评论了你的动态",
                previewText(commentContentText),
                postId,
                commentId
            ));
        }

        if (replyToUserId != null
            && !Objects.equals(replyToUserId, actorUserId)
            && !Objects.equals(replyToUserId, postAuthorUserId)) {
            pendingNotifications.add(buildNotification(
                replyToUserId,
                NotificationType.COMMENT,
                actorUserId,
                actorName,
                actorName + " 回复了你的评论",
                previewText(commentContentText),
                postId,
                commentId
            ));
        }

        if (pendingNotifications.isEmpty()) {
            return;
        }

        pendingNotifications.forEach(userNotificationMapper::insert);
        registerAfterCommit(() -> pendingNotifications.forEach(this::emitCreatedNotification));
    }

    @Transactional
    public void broadcastToUsers(String senderName, String title, String contentText) {
        List<UserAccount> activeUsers = userAccountMapper.selectList(new LambdaQueryWrapper<UserAccount>()
            .eq(UserAccount::getStatus, 1));
        if (activeUsers.isEmpty()) {
            return;
        }

        List<UserNotification> notifications = activeUsers.stream()
            // V1 广播先按“每个用户一条站内信”落库，链路简单直观，后续要做广播历史或大规模分发再继续演进。
            .map(userAccount -> buildNotification(
                userAccount.getId(),
                NotificationType.BROADCAST,
                null,
                senderName,
                title.trim(),
                contentText.trim(),
                null,
                null
            ))
            .toList();
        notifications.forEach(userNotificationMapper::insert);
        registerAfterCommit(() -> notifications.forEach(this::emitCreatedNotification));
    }

    public PageResponse<UserNotificationResponse> listNotifications(Long receiverUserId, int pageNo, int pageSize) {
        Page<UserNotification> page = userNotificationMapper.selectPage(
            new Page<>(pageNo, pageSize),
            new LambdaQueryWrapper<UserNotification>()
                .eq(UserNotification::getReceiverUserId, receiverUserId)
                .orderByDesc(UserNotification::getCreatedAt)
                .orderByDesc(UserNotification::getId)
        );
        List<UserNotificationResponse> items = mapToResponses(page.getRecords());
        return new PageResponse<>(items, page.getTotal(), pageNo, pageSize);
    }

    public UnreadCountResponse unreadCount(Long receiverUserId) {
        return new UnreadCountResponse(countUnread(receiverUserId));
    }

    @Transactional
    public void markRead(Long receiverUserId, Long notificationId) {
        LocalDateTime now = LocalDateTime.now();
        userNotificationMapper.update(
            null,
            new LambdaUpdateWrapper<UserNotification>()
                .eq(UserNotification::getId, notificationId)
                .eq(UserNotification::getReceiverUserId, receiverUserId)
                .eq(UserNotification::getIsRead, 0)
                .set(UserNotification::getIsRead, 1)
                .set(UserNotification::getReadAt, now)
        );
    }

    @Transactional
    public void markAllRead(Long receiverUserId) {
        userNotificationMapper.markAllAsRead(receiverUserId, LocalDateTime.now());
    }

    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter connect(Long receiverUserId) {
        NotificationStreamEventResponse snapshotEvent = new NotificationStreamEventResponse(
            NotificationEventType.SNAPSHOT,
            countUnread(receiverUserId),
            null
        );
        return notificationStreamHub.connect(receiverUserId, snapshotEvent);
    }

    private void persistAndPushAfterCommit(UserNotification notification) {
        userNotificationMapper.insert(notification);
        // 站内信必须等事务真正提交成功后再发 SSE。
        // 否则点赞/评论事务回滚时，前端仍可能先收到一条“幽灵消息”。
        registerAfterCommit(() -> emitCreatedNotification(notification));
    }

    private void emitCreatedNotification(UserNotification notification) {
        notificationStreamHub.emit(
            notification.getReceiverUserId(),
            new NotificationStreamEventResponse(
                NotificationEventType.NOTIFICATION_CREATED,
                countUnread(notification.getReceiverUserId()),
                mapToResponses(List.of(notification)).stream().findFirst().orElse(null)
            )
        );
    }

    private long countUnread(Long receiverUserId) {
        return userNotificationMapper.selectCount(new LambdaQueryWrapper<UserNotification>()
            .eq(UserNotification::getReceiverUserId, receiverUserId)
            .eq(UserNotification::getIsRead, 0));
    }

    private List<UserNotificationResponse> mapToResponses(Collection<UserNotification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return List.of();
        }

        List<Long> actorUserIds = notifications.stream()
            .map(UserNotification::getActorUserId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, UserAccount> accountMap = actorUserIds.isEmpty()
            ? Map.of()
            : userAccountMapper.selectBatchIds(actorUserIds).stream()
                .collect(Collectors.toMap(UserAccount::getId, Function.identity()));
        Map<Long, UserProfile> profileMap = actorUserIds.isEmpty()
            ? Map.of()
            : userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                    .in(UserProfile::getUserId, actorUserIds))
                .stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        return notifications.stream()
            .map(notification -> toResponse(notification, accountMap, profileMap))
            .toList();
    }

    private UserNotificationResponse toResponse(
        UserNotification notification,
        Map<Long, UserAccount> accountMap,
        Map<Long, UserProfile> profileMap
    ) {
        Long senderUserId = notification.getActorUserId();
        UserAccount actorAccount = senderUserId == null ? null : accountMap.get(senderUserId);
        UserProfile actorProfile = senderUserId == null ? null : profileMap.get(senderUserId);
        String senderName = StringUtils.hasText(notification.getSenderName())
            ? notification.getSenderName()
            : resolveDisplayName(actorAccount, actorProfile);
        String senderAvatarUrl = actorProfile != null && StringUtils.hasText(actorProfile.getAvatarUrl())
            ? actorProfile.getAvatarUrl()
            : "";

        return new UserNotificationResponse(
            notification.getId(),
            notification.getNotificationType(),
            senderUserId,
            senderName,
            senderAvatarUrl,
            notification.getTitle(),
            notification.getContentText(),
            notification.getIsRead() != null && notification.getIsRead() == 1,
            notification.getCreatedAt(),
            notification.getPostId(),
            notification.getCommentId()
        );
    }

    private UserNotification buildNotification(
        Long receiverUserId,
        String notificationType,
        Long actorUserId,
        String senderName,
        String title,
        String contentText,
        Long postId,
        Long commentId
    ) {
        UserNotification notification = new UserNotification();
        notification.setReceiverUserId(receiverUserId);
        notification.setNotificationType(notificationType);
        notification.setActorUserId(actorUserId);
        notification.setSenderName(StringUtils.hasText(senderName) ? senderName.trim() : "");
        notification.setTitle(title.trim());
        notification.setContentText(StringUtils.hasText(contentText) ? contentText.trim() : "");
        notification.setPostId(postId);
        notification.setCommentId(commentId);
        notification.setIsRead(0);
        notification.setReadAt(null);
        return notification;
    }

    private String senderName(Long actorUserId) {
        Map<Long, String> names = resolveSenderNames(List.of(actorUserId));
        return names.getOrDefault(actorUserId, "用户");
    }

    private Map<Long, String> resolveSenderNames(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, UserAccount> accountMap = userAccountMapper.selectBatchIds(userIds).stream()
            .collect(Collectors.toMap(UserAccount::getId, Function.identity()));
        Map<Long, UserProfile> profileMap = userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                .in(UserProfile::getUserId, userIds))
            .stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
        Map<Long, String> names = new LinkedHashMap<>();
        userIds.forEach(userId -> names.put(userId, resolveDisplayName(accountMap.get(userId), profileMap.get(userId))));
        return names;
    }

    private String resolveDisplayName(UserAccount account, UserProfile profile) {
        if (profile != null && StringUtils.hasText(profile.getNickname())) {
            return profile.getNickname().trim();
        }
        if (account != null && StringUtils.hasText(account.getUsername())) {
            return account.getUsername().trim();
        }
        return "用户";
    }

    private String previewText(String contentText) {
        if (!StringUtils.hasText(contentText)) {
            return "";
        }
        String normalized = contentText
            .replace("\r", " ")
            .replace("\n", " ")
            .trim()
            .replaceAll("\\s{2,}", " ");
        if (normalized.length() <= DEFAULT_PREVIEW_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, DEFAULT_PREVIEW_LENGTH) + "...";
    }

    private void registerAfterCommit(Runnable runnable) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            });
            return;
        }
        runnable.run();
    }
}
