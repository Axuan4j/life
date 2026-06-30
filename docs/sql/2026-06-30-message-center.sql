USE `life`;

CREATE TABLE IF NOT EXISTS `user_notification`
(
    `id`                BIGINT       NOT NULL,
    `receiver_user_id`  BIGINT       NOT NULL,
    `notification_type` VARCHAR(32)  NOT NULL,
    `actor_user_id`     BIGINT       NULL,
    `sender_name`       VARCHAR(64)  NOT NULL DEFAULT '',
    `title`             VARCHAR(128) NOT NULL,
    `content_text`      VARCHAR(500) NOT NULL DEFAULT '',
    `post_id`           BIGINT       NULL,
    `comment_id`        BIGINT       NULL,
    `is_read`           TINYINT      NOT NULL DEFAULT 0,
    `read_at`           DATETIME     NULL,
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_notification_receiver_created` (`receiver_user_id`, `created_at`, `id`),
    KEY `idx_user_notification_receiver_read` (`receiver_user_id`, `is_read`, `created_at`, `id`),
    KEY `idx_user_notification_actor_user_id` (`actor_user_id`),
    CONSTRAINT `fk_user_notification_receiver_user_id` FOREIGN KEY (`receiver_user_id`) REFERENCES `user_account` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_notification_actor_user_id` FOREIGN KEY (`actor_user_id`) REFERENCES `user_account` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_user_notification_post_id` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_user_notification_comment_id` FOREIGN KEY (`comment_id`) REFERENCES `post_comment` (`id`) ON DELETE SET NULL
);

INSERT INTO `admin_menu`
(`id`, `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`, `view_key`, `icon_name`, `permission_code`, `sort_order`, `visible`, `status`, `is_system`)
VALUES
  (82004, 0, 'PAGE', '消息广播', 'message-broadcast', '/messages/broadcast', 'BroadcastManageView', 'message', 'admin:message:broadcast', 35, 1, 1, 1)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `menu_type` = VALUES(`menu_type`),
  `menu_name` = VALUES(`menu_name`),
  `route_name` = VALUES(`route_name`),
  `route_path` = VALUES(`route_path`),
  `view_key` = VALUES(`view_key`),
  `icon_name` = VALUES(`icon_name`),
  `permission_code` = VALUES(`permission_code`),
  `sort_order` = VALUES(`sort_order`),
  `visible` = VALUES(`visible`),
  `status` = VALUES(`status`),
  `is_system` = VALUES(`is_system`);

INSERT INTO `admin_role_menu` (`id`, `role_id`, `menu_id`)
VALUES
  (84004, 81001, 82004)
ON DUPLICATE KEY UPDATE
  `role_id` = VALUES(`role_id`),
  `menu_id` = VALUES(`menu_id`);
