-- 这份初始化脚本面向本地开发环境，目标是让后端、H5、管理端都能直接连接同一套基础设施。

CREATE DATABASE IF NOT EXISTS `life`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'life'@'%' IDENTIFIED BY 'life123456';
GRANT ALL PRIVILEGES ON `life`.* TO 'life'@'%';
FLUSH PRIVILEGES;

USE `life`;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user_account`
(
    `id`                BIGINT       NOT NULL,
    `username`          VARCHAR(64)  NOT NULL,
    `password_hash`     VARCHAR(255) NOT NULL,
    `role_code`         VARCHAR(32)  NOT NULL DEFAULT 'USER',
    `status`            TINYINT      NOT NULL DEFAULT 1,
    `last_login_ip`     VARCHAR(64)  NOT NULL DEFAULT '',
    `last_login_region` VARCHAR(128) NOT NULL DEFAULT '',
    `last_login_at`     DATETIME     NULL,
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='C 端用户账户表';

CREATE TABLE IF NOT EXISTS `user_profile`
(
    `id`         BIGINT       NOT NULL,
    `user_id`    BIGINT       NOT NULL,
    `nickname`   VARCHAR(64)  NOT NULL,
    `avatar_url` VARCHAR(255) NOT NULL DEFAULT '',
    `bio`        VARCHAR(255) NOT NULL DEFAULT '',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_profile_user_id` (`user_id`),
    CONSTRAINT `fk_user_profile_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户资料表';

CREATE TABLE IF NOT EXISTS `admin_account`
(
    `id`                BIGINT       NOT NULL,
    `username`          VARCHAR(64)  NOT NULL,
    `display_name`      VARCHAR(64)  NOT NULL DEFAULT '',
    `password_hash`     VARCHAR(255) NOT NULL,
    `status`            TINYINT      NOT NULL DEFAULT 1,
    `last_login_ip`     VARCHAR(64)  NOT NULL DEFAULT '',
    `last_login_region` VARCHAR(128) NOT NULL DEFAULT '',
    `last_login_at`     DATETIME     NULL,
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_account_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='后台管理员账户表';

CREATE TABLE IF NOT EXISTS `admin_role`
(
    `id`         BIGINT       NOT NULL,
    `role_code`  VARCHAR(64)  NOT NULL,
    `role_name`  VARCHAR(64)  NOT NULL,
    `status`     TINYINT      NOT NULL DEFAULT 1,
    `remark`     VARCHAR(255) NOT NULL DEFAULT '',
    `is_system`  TINYINT      NOT NULL DEFAULT 0,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_role_code` (`role_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='后台角色表';

CREATE TABLE IF NOT EXISTS `admin_menu`
(
    `id`              BIGINT       NOT NULL,
    `parent_id`       BIGINT       NOT NULL DEFAULT 0,
    `menu_type`       VARCHAR(32)  NOT NULL,
    `menu_name`       VARCHAR(64)  NOT NULL,
    `route_name`      VARCHAR(64)  NOT NULL,
    `route_path`      VARCHAR(255) NOT NULL,
    `view_key`        VARCHAR(128) NULL DEFAULT NULL,
    `icon_name`       VARCHAR(64)  NOT NULL DEFAULT '',
    `permission_code` VARCHAR(128) NULL DEFAULT NULL,
    `sort_order`      INT          NOT NULL DEFAULT 0,
    `visible`         TINYINT      NOT NULL DEFAULT 1,
    `status`          TINYINT      NOT NULL DEFAULT 1,
    `is_system`       TINYINT      NOT NULL DEFAULT 0,
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_menu_route_name` (`route_name`),
    UNIQUE KEY `uk_admin_menu_permission_code` (`permission_code`),
    KEY `idx_admin_menu_parent_sort` (`parent_id`, `sort_order`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='后台菜单表';

CREATE TABLE IF NOT EXISTS `admin_user_role`
(
    `id`         BIGINT   NOT NULL,
    `user_id`    BIGINT   NOT NULL,
    `role_id`    BIGINT   NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_user_role_pair` (`user_id`, `role_id`),
    KEY `idx_admin_user_role_role_id` (`role_id`),
    CONSTRAINT `fk_admin_user_role_user_id` FOREIGN KEY (`user_id`) REFERENCES `admin_account` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_admin_user_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `admin_role` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='后台管理员角色绑定表';

CREATE TABLE IF NOT EXISTS `admin_role_menu`
(
    `id`         BIGINT   NOT NULL,
    `role_id`    BIGINT   NOT NULL,
    `menu_id`    BIGINT   NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_role_menu_pair` (`role_id`, `menu_id`),
    KEY `idx_admin_role_menu_menu_id` (`menu_id`),
    CONSTRAINT `fk_admin_role_menu_role_id` FOREIGN KEY (`role_id`) REFERENCES `admin_role` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_admin_role_menu_menu_id` FOREIGN KEY (`menu_id`) REFERENCES `admin_menu` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='后台角色菜单绑定表';

CREATE TABLE IF NOT EXISTS `post`
(
    `id`           BIGINT        NOT NULL,
    `author_id`    BIGINT        NOT NULL,
    `content_text` VARCHAR(2000) NOT NULL,
    `visibility`   VARCHAR(32)   NOT NULL DEFAULT 'PUBLIC',
    `status`       VARCHAR(32)   NOT NULL DEFAULT 'PUBLISHED',
    `client_ip`    VARCHAR(64)   NOT NULL DEFAULT '',
    `ip_region`    VARCHAR(128)  NOT NULL DEFAULT '',
    `published_at` DATETIME      NOT NULL,
    `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_post_author_published` (`author_id`, `published_at` DESC, `id` DESC),
    KEY `idx_post_status_visibility_published` (`status`, `visibility`, `published_at` DESC, `id` DESC),
    CONSTRAINT `fk_post_author_id` FOREIGN KEY (`author_id`) REFERENCES `user_account` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='帖子主表';

CREATE TABLE IF NOT EXISTS `post_media`
(
    `id`         BIGINT       NOT NULL,
    `post_id`    BIGINT       NOT NULL,
    `media_type` VARCHAR(32)  NOT NULL,
    `media_url`  VARCHAR(255) NOT NULL,
    `sort_order` INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_post_media_post_sort` (`post_id`, `sort_order`),
    CONSTRAINT `fk_post_media_post_id` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='帖子媒体表';

CREATE TABLE IF NOT EXISTS `post_stat`
(
    `post_id`       BIGINT NOT NULL,
    `like_count`    BIGINT NOT NULL DEFAULT 0,
    `comment_count` BIGINT NOT NULL DEFAULT 0,
    `repost_count`  BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`post_id`),
    CONSTRAINT `fk_post_stat_post_id` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='帖子统计表';

CREATE TABLE IF NOT EXISTS `post_comment`
(
    `id`                BIGINT       NOT NULL,
    `post_id`           BIGINT       NOT NULL,
    `user_id`           BIGINT       NOT NULL,
    `parent_comment_id` BIGINT       NULL,
    `reply_to_user_id`  BIGINT       NULL,
    `content_text`      VARCHAR(500) NOT NULL,
    `status`            VARCHAR(32)  NOT NULL DEFAULT 'VISIBLE',
    `client_ip`         VARCHAR(64)  NOT NULL DEFAULT '',
    `ip_region`         VARCHAR(128) NOT NULL DEFAULT '',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_post_comment_post_created` (`post_id`, `created_at`, `id`),
    KEY `idx_post_comment_parent` (`parent_comment_id`),
    CONSTRAINT `fk_post_comment_post_id` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_post_comment_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_post_comment_parent_id` FOREIGN KEY (`parent_comment_id`) REFERENCES `post_comment` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='帖子评论表';

CREATE TABLE IF NOT EXISTS `post_like`
(
    `id`         BIGINT   NOT NULL,
    `post_id`    BIGINT   NOT NULL,
    `user_id`    BIGINT   NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_like_pair` (`post_id`, `user_id`),
    KEY `idx_post_like_post_created` (`post_id`, `created_at`, `id`),
    CONSTRAINT `fk_post_like_post_id` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_post_like_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='帖子点赞表';

CREATE TABLE IF NOT EXISTS `post_repost`
(
    `id`             BIGINT   NOT NULL,
    `post_id`        BIGINT   NOT NULL,
    `user_id`        BIGINT   NOT NULL,
    `repost_post_id` BIGINT   NOT NULL,
    `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_repost_pair` (`post_id`, `user_id`),
    KEY `idx_post_repost_post_created` (`post_id`, `created_at`, `id`),
    CONSTRAINT `fk_post_repost_post_id` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_post_repost_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_post_repost_created_post_id` FOREIGN KEY (`repost_post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='帖子转发表';

CREATE TABLE IF NOT EXISTS `user_follow`
(
    `id`               BIGINT   NOT NULL,
    `follower_user_id` BIGINT   NOT NULL,
    `followed_user_id` BIGINT   NOT NULL,
    `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follow_pair` (`follower_user_id`, `followed_user_id`),
    KEY `idx_follow_followed_user_id` (`followed_user_id`),
    CONSTRAINT `fk_user_follow_follower` FOREIGN KEY (`follower_user_id`) REFERENCES `user_account` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_follow_followed` FOREIGN KEY (`followed_user_id`) REFERENCES `user_account` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='关注关系表';

CREATE TABLE IF NOT EXISTS `feed_exposure`
(
    `id`             BIGINT       NOT NULL,
    `viewer_user_id` BIGINT       NOT NULL,
    `post_id`        BIGINT       NOT NULL,
    `source_type`    VARCHAR(32)  NOT NULL,
    `cursor_token`   VARCHAR(128) NOT NULL DEFAULT '',
    `shown_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_feed_exposure_viewer_shown` (`viewer_user_id`, `shown_at` DESC),
    KEY `idx_feed_exposure_post_id` (`post_id`),
    CONSTRAINT `fk_feed_exposure_viewer` FOREIGN KEY (`viewer_user_id`) REFERENCES `user_account` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_feed_exposure_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='Feed 曝光记录表';

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
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户站内信与互动通知表';
