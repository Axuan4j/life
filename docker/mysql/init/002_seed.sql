USE `life`;

-- 这些种子数据主要用于本地把登录、发帖、关注、首页 Feed 直接跑起来。

INSERT INTO `user_account` (`id`, `username`, `password_hash`, `role_code`, `status`)
VALUES
  (10001, 'admin', '$2a$10$vR/uq5oqMMWsZS7k64/A7.BIEqD1CC27Yge8EsIIHbHU4.ETRTr4.', 'ADMIN', 1),
  (10002, 'life_user', '$2a$10$I3RyWviDNchegt2M5H1PaehWvFKWO7BLvfBaiQvPtj0KOt9v.RgVS', 'USER', 1),
  (10003, 'alice', '$2a$10$I3RyWviDNchegt2M5H1PaehWvFKWO7BLvfBaiQvPtj0KOt9v.RgVS', 'USER', 1),
  (10004, 'bob', '$2a$10$I3RyWviDNchegt2M5H1PaehWvFKWO7BLvfBaiQvPtj0KOt9v.RgVS', 'USER', 1)
ON DUPLICATE KEY UPDATE
  `password_hash` = VALUES(`password_hash`),
  `role_code` = VALUES(`role_code`),
  `status` = VALUES(`status`);

UPDATE `user_account`
SET
  `last_login_ip` = '127.0.0.1',
  `last_login_region` = '本地',
  `last_login_at` = NOW()
WHERE `id` IN (10001, 10002, 10003, 10004);

INSERT INTO `user_profile` (`id`, `user_id`, `nickname`, `avatar_url`, `bio`)
VALUES
  (20001, 10001, 'Life 管理员', '', '负责后台管理和基础运营配置'),
  (20002, 10002, 'Life 用户', '', '这是一个用于联调 Feed 的普通测试账号'),
  (20003, 10003, 'Alice', '', '分享产品设计、旅行和生活碎片'),
  (20004, 10004, 'Bob', '', '关注技术、骑行和轻内容表达')
ON DUPLICATE KEY UPDATE
  `nickname` = VALUES(`nickname`),
  `avatar_url` = VALUES(`avatar_url`),
  `bio` = VALUES(`bio`);

INSERT INTO `post` (`id`, `author_id`, `content_text`, `visibility`, `status`, `client_ip`, `ip_region`, `published_at`)
VALUES
  (30001, 10003, '今天把新版本的发帖流程重新梳理了一遍，越简单的产品越需要把细节磨好。', 'PUBLIC', 'PUBLISHED', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
  (30002, 10004, '清晨骑行完回来喝一杯冰美式，再开始写代码，效率会高很多。', 'PUBLIC', 'PUBLISHED', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 35 MINUTE)),
  (30003, 10003, '如果首页 feed 以关注内容为主，推荐内容就更像“打断式发现”，节奏一定要克制。', 'PUBLIC', 'PUBLISHED', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 55 MINUTE)),
  (30004, 10004, '给移动端 feed 做 cursor 分页，比 pageNo/pageSize 更适合动态流场景。', 'PUBLIC', 'PUBLISHED', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 70 MINUTE))
ON DUPLICATE KEY UPDATE
  `content_text` = VALUES(`content_text`),
  `client_ip` = VALUES(`client_ip`),
  `ip_region` = VALUES(`ip_region`),
  `published_at` = VALUES(`published_at`);

INSERT INTO `post_stat` (`post_id`, `like_count`, `comment_count`, `repost_count`)
VALUES
  (30001, 12, 3, 1),
  (30002, 8, 2, 0),
  (30003, 21, 5, 2),
  (30004, 16, 4, 1)
ON DUPLICATE KEY UPDATE
  `like_count` = VALUES(`like_count`),
  `comment_count` = VALUES(`comment_count`),
  `repost_count` = VALUES(`repost_count`);

INSERT INTO `post_comment` (`id`, `post_id`, `user_id`, `parent_comment_id`, `reply_to_user_id`, `content_text`, `status`, `client_ip`, `ip_region`, `created_at`)
VALUES
  (50001, 30001, 10002, NULL, NULL, '这版发帖页的节奏很舒服，主操作够聚焦。', 'VISIBLE', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 14 MINUTE)),
  (50002, 30001, 10004, 50001, 10002, '同意，尤其是把复杂设置先收起来这一点很对。', 'VISIBLE', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 11 MINUTE)),
  (50003, 30001, 10003, NULL, NULL, '接下来可以把图片和短视频入口再区分得更明确一些。', 'VISIBLE', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 9 MINUTE)),
  (50004, 30002, 10002, NULL, NULL, '骑行完开始写代码这个状态我也很熟。', 'VISIBLE', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 19 MINUTE)),
  (50005, 30002, 10004, 50004, 10002, '早上脑子清醒，特别适合做结构化工作。', 'VISIBLE', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 16 MINUTE)),
  (50006, 30003, 10002, NULL, NULL, '推荐内容占比克制这一点很重要，不然首页会很跳。', 'VISIBLE', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 41 MINUTE))
ON DUPLICATE KEY UPDATE
  `content_text` = VALUES(`content_text`),
  `status` = VALUES(`status`),
  `reply_to_user_id` = VALUES(`reply_to_user_id`),
  `client_ip` = VALUES(`client_ip`),
  `ip_region` = VALUES(`ip_region`),
  `created_at` = VALUES(`created_at`);

INSERT INTO `post_like` (`id`, `post_id`, `user_id`, `created_at`)
VALUES
  (60001, 30001, 10002, DATE_SUB(NOW(), INTERVAL 13 MINUTE)),
  (60002, 30001, 10004, DATE_SUB(NOW(), INTERVAL 12 MINUTE)),
  (60003, 30001, 10003, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
  (60004, 30002, 10002, DATE_SUB(NOW(), INTERVAL 18 MINUTE)),
  (60005, 30003, 10002, DATE_SUB(NOW(), INTERVAL 37 MINUTE)),
  (60006, 30003, 10004, DATE_SUB(NOW(), INTERVAL 35 MINUTE))
ON DUPLICATE KEY UPDATE
  `created_at` = VALUES(`created_at`);

INSERT INTO `post` (`id`, `author_id`, `content_text`, `visibility`, `status`, `client_ip`, `ip_region`, `published_at`)
VALUES
  (30005, 10004, '转发 @Alice：\n今天把新版本的发帖流程重新梳理了一遍，越简单的产品越需要把细节磨好。', 'PUBLIC', 'PUBLISHED', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 8 MINUTE)),
  (30006, 10002, '转发 @Alice：\n如果首页 feed 以关注内容为主，推荐内容就更像“打断式发现”，节奏一定要克制。', 'PUBLIC', 'PUBLISHED', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 30 MINUTE))
ON DUPLICATE KEY UPDATE
  `content_text` = VALUES(`content_text`),
  `client_ip` = VALUES(`client_ip`),
  `ip_region` = VALUES(`ip_region`),
  `published_at` = VALUES(`published_at`);

INSERT INTO `post_stat` (`post_id`, `like_count`, `comment_count`, `repost_count`)
VALUES
  (30005, 0, 0, 0),
  (30006, 0, 0, 0)
ON DUPLICATE KEY UPDATE
  `like_count` = VALUES(`like_count`),
  `comment_count` = VALUES(`comment_count`),
  `repost_count` = VALUES(`repost_count`);

INSERT INTO `post_repost` (`id`, `post_id`, `user_id`, `repost_post_id`, `created_at`)
VALUES
  (70001, 30001, 10004, 30005, DATE_SUB(NOW(), INTERVAL 8 MINUTE)),
  (70002, 30003, 10002, 30006, DATE_SUB(NOW(), INTERVAL 30 MINUTE))
ON DUPLICATE KEY UPDATE
  `repost_post_id` = VALUES(`repost_post_id`),
  `created_at` = VALUES(`created_at`);

INSERT INTO `user_follow` (`id`, `follower_user_id`, `followed_user_id`)
VALUES
  (40001, 10002, 10003),
  (40002, 10002, 10004)
ON DUPLICATE KEY UPDATE
  `follower_user_id` = VALUES(`follower_user_id`),
  `followed_user_id` = VALUES(`followed_user_id`);
