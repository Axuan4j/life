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
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='账户表';

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

ALTER TABLE `user_account`
    ADD COLUMN `last_login_ip`     VARCHAR(64)  NOT NULL DEFAULT '' AFTER `status`,
    ADD COLUMN `last_login_region` VARCHAR(128) NOT NULL DEFAULT '' AFTER `last_login_ip`,
    ADD COLUMN `last_login_at`     DATETIME     NULL AFTER `last_login_region`;

ALTER TABLE `post`
    ADD COLUMN `client_ip` VARCHAR(64)  NOT NULL DEFAULT '' AFTER `status`,
    ADD COLUMN `ip_region` VARCHAR(128) NOT NULL DEFAULT '' AFTER `client_ip`;

ALTER TABLE `post_comment`
    ADD COLUMN `client_ip` VARCHAR(64)  NOT NULL DEFAULT '' AFTER `status`,
    ADD COLUMN `ip_region` VARCHAR(128) NOT NULL DEFAULT '' AFTER `client_ip`;

ALTER TABLE `post_repost`
    ADD COLUMN `repost_post_id` BIGINT NOT NULL DEFAULT 0 AFTER `user_id`;
