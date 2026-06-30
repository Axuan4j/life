USE `life`;

SET NAMES utf8mb4;

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

INSERT INTO `admin_account` (`id`, `username`, `display_name`, `password_hash`, `status`, `last_login_ip`, `last_login_region`, `last_login_at`)
SELECT
  account.`id`,
  `username`,
  COALESCE(profile.`nickname`, account.`username`) AS `display_name`,
  `password_hash`,
  `status`,
  `last_login_ip`,
  `last_login_region`,
  `last_login_at`
FROM `user_account` account
LEFT JOIN `user_profile` profile ON profile.`user_id` = account.`id`
WHERE account.`role_code` = 'ADMIN'
ON DUPLICATE KEY UPDATE
  `display_name` = VALUES(`display_name`),
  `password_hash` = VALUES(`password_hash`),
  `status` = VALUES(`status`),
  `last_login_ip` = VALUES(`last_login_ip`),
  `last_login_region` = VALUES(`last_login_region`),
  `last_login_at` = VALUES(`last_login_at`);

ALTER TABLE `admin_user_role`
    DROP FOREIGN KEY `fk_admin_user_role_user_id`;

ALTER TABLE `admin_user_role`
    ADD CONSTRAINT `fk_admin_user_role_user_id`
        FOREIGN KEY (`user_id`) REFERENCES `admin_account` (`id`) ON DELETE CASCADE;

DELETE profile
FROM `user_profile` profile
INNER JOIN `user_account` account ON account.`id` = profile.`user_id`
WHERE account.`role_code` = 'ADMIN';

DELETE FROM `user_account`
WHERE `role_code` = 'ADMIN';
