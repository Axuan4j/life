USE `life`;

-- 这些种子数据主要用于本地把登录、发帖、关注、首页 Feed 直接跑起来。

INSERT INTO `user_account` (`id`, `username`, `password_hash`, `role_code`, `status`)
VALUES
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
WHERE `id` IN (10002, 10003, 10004);

INSERT INTO `user_profile` (`id`, `user_id`, `nickname`, `avatar_url`, `bio`)
VALUES
  (20002, 10002, 'Life 用户', '', '这是一个用于联调 Feed 的普通测试账号'),
  (20003, 10003, 'Alice', '', '分享产品设计、旅行和生活碎片'),
  (20004, 10004, 'Bob', '', '关注技术、骑行和轻内容表达')
ON DUPLICATE KEY UPDATE
  `nickname` = VALUES(`nickname`),
  `avatar_url` = VALUES(`avatar_url`),
  `bio` = VALUES(`bio`);

INSERT INTO `admin_account` (`id`, `username`, `display_name`, `password_hash`, `status`)
VALUES
  (90001, 'admin', 'Life 管理员', '$2a$10$vR/uq5oqMMWsZS7k64/A7.BIEqD1CC27Yge8EsIIHbHU4.ETRTr4.', 1)
ON DUPLICATE KEY UPDATE
  `display_name` = VALUES(`display_name`),
  `password_hash` = VALUES(`password_hash`),
  `status` = VALUES(`status`);

UPDATE `admin_account`
SET
  `last_login_ip` = '127.0.0.1',
  `last_login_region` = '本地',
  `last_login_at` = NOW()
WHERE `id` IN (90001);

INSERT INTO `admin_role` (`id`, `role_code`, `role_name`, `status`, `remark`, `is_system`)
VALUES
  (81001, 'SUPER_ADMIN', '超级管理员', 1, '拥有全部后台菜单与页面权限', 1),
  (81002, 'USER_ADMIN', '用户管理员', 1, '负责用户查看和基础管理', 1),
  (81003, 'CONTENT_ADMIN', '内容管理员', 1, '负责帖子查看和内容维护', 1)
ON DUPLICATE KEY UPDATE
  `role_name` = VALUES(`role_name`),
  `status` = VALUES(`status`),
  `remark` = VALUES(`remark`),
  `is_system` = VALUES(`is_system`);

INSERT INTO `admin_menu`
(`id`, `parent_id`, `menu_type`, `menu_name`, `route_name`, `route_path`, `view_key`, `icon_name`, `permission_code`, `sort_order`, `visible`, `status`, `is_system`)
VALUES
  (82001, 0, 'PAGE', '概览', 'overview', '/overview', 'OverviewView', 'dashboard', 'admin:overview', 10, 1, 1, 1),
  (82002, 0, 'PAGE', '用户管理', 'users', '/users', 'UserListView', 'user', 'admin:user', 20, 1, 1, 1),
  (82003, 0, 'PAGE', '帖子管理', 'posts', '/posts', 'PostListView', 'content', 'admin:post', 30, 1, 1, 1),
  (82004, 0, 'PAGE', '消息广播', 'message-broadcast', '/messages/broadcast', 'BroadcastManageView', 'message', 'admin:message:broadcast', 35, 1, 1, 1),
  (82010, 0, 'DIRECTORY', '权限管理', 'rbac', '/rbac', NULL, 'shield', NULL, 40, 1, 1, 1),
  (82011, 82010, 'PAGE', '菜单管理', 'rbac-menu', '/rbac/menus', 'MenuManageView', 'menu', 'admin:rbac:menu', 41, 1, 1, 1),
  (82012, 82010, 'PAGE', '角色管理', 'rbac-role', '/rbac/roles', 'RoleManageView', 'role', 'admin:rbac:role', 42, 1, 1, 1),
  (82013, 82010, 'PAGE', '管理员角色', 'rbac-admin-users', '/rbac/admin-users', 'AdminUserRoleView', 'account', 'admin:rbac:account', 43, 1, 1, 1)
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

INSERT INTO `admin_user_role` (`id`, `user_id`, `role_id`)
VALUES
  (83001, 90001, 81001)
ON DUPLICATE KEY UPDATE
  `user_id` = VALUES(`user_id`),
  `role_id` = VALUES(`role_id`);

INSERT INTO `admin_role_menu` (`id`, `role_id`, `menu_id`)
VALUES
  (84001, 81001, 82001),
  (84002, 81001, 82002),
  (84003, 81001, 82003),
  (84004, 81001, 82004),
  (84005, 81001, 82010),
  (84006, 81001, 82011),
  (84007, 81001, 82012),
  (84008, 81001, 82013),
  (84009, 81002, 82001),
  (84010, 81002, 82002),
  (84011, 81003, 82001),
  (84012, 81003, 82003)
ON DUPLICATE KEY UPDATE
  `role_id` = VALUES(`role_id`),
  `menu_id` = VALUES(`menu_id`);

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
  (30006, 10002, '转发 @Alice：\n如果首页 feed 以关注内容为主，推荐内容就更像“打断式发现”，节奏一定要克制。', 'PUBLIC', 'PUBLISHED', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
  (30007, 10003, '转发 @Life 用户：\n转发 @Alice：\n如果首页 feed 以关注内容为主，推荐内容就更像“打断式发现”，节奏一定要克制。', 'PUBLIC', 'PUBLISHED', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 7 MINUTE))
ON DUPLICATE KEY UPDATE
  `content_text` = VALUES(`content_text`),
  `client_ip` = VALUES(`client_ip`),
  `ip_region` = VALUES(`ip_region`),
  `published_at` = VALUES(`published_at`);

INSERT INTO `post_stat` (`post_id`, `like_count`, `comment_count`, `repost_count`)
VALUES
  (30005, 0, 0, 0),
  (30006, 1, 1, 1),
  (30007, 0, 0, 0)
ON DUPLICATE KEY UPDATE
  `like_count` = VALUES(`like_count`),
  `comment_count` = VALUES(`comment_count`),
  `repost_count` = VALUES(`repost_count`);

INSERT INTO `post_repost` (`id`, `post_id`, `user_id`, `repost_post_id`, `created_at`)
VALUES
  (70001, 30001, 10004, 30005, DATE_SUB(NOW(), INTERVAL 8 MINUTE)),
  (70002, 30003, 10002, 30006, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
  (70003, 30006, 10003, 30007, DATE_SUB(NOW(), INTERVAL 7 MINUTE))
ON DUPLICATE KEY UPDATE
  `repost_post_id` = VALUES(`repost_post_id`),
  `created_at` = VALUES(`created_at`);

INSERT INTO `post_comment` (`id`, `post_id`, `user_id`, `parent_comment_id`, `reply_to_user_id`, `content_text`, `status`, `client_ip`, `ip_region`, `created_at`)
VALUES
  (50007, 30006, 10004, NULL, NULL, '这条转发很有共鸣，推荐和关注的比例确实要克制。', 'VISIBLE', '127.0.0.1', '本地', DATE_SUB(NOW(), INTERVAL 6 MINUTE))
ON DUPLICATE KEY UPDATE
  `content_text` = VALUES(`content_text`),
  `status` = VALUES(`status`),
  `reply_to_user_id` = VALUES(`reply_to_user_id`),
  `client_ip` = VALUES(`client_ip`),
  `ip_region` = VALUES(`ip_region`),
  `created_at` = VALUES(`created_at`);

INSERT INTO `post_like` (`id`, `post_id`, `user_id`, `created_at`)
VALUES
  (60007, 30006, 10003, DATE_SUB(NOW(), INTERVAL 5 MINUTE))
ON DUPLICATE KEY UPDATE
  `created_at` = VALUES(`created_at`);

INSERT INTO `user_follow` (`id`, `follower_user_id`, `followed_user_id`)
VALUES
  (40001, 10002, 10003),
  (40002, 10002, 10004)
ON DUPLICATE KEY UPDATE
  `follower_user_id` = VALUES(`follower_user_id`),
  `followed_user_id` = VALUES(`followed_user_id`);

INSERT INTO `user_notification`
(`id`, `receiver_user_id`, `notification_type`, `actor_user_id`, `sender_name`, `title`, `content_text`, `post_id`, `comment_id`, `is_read`, `read_at`, `created_at`)
VALUES
  (85001, 10002, 'LIKE', 10003, 'Alice', 'Alice 赞了你的动态', '转发 @Alice： 如果首页 feed 以关注内容为主，推荐内容就更像“打断式发现”，节奏一定要克制。', 30006, NULL, 0, NULL, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
  (85002, 10002, 'COMMENT', 10004, 'Bob', 'Bob 评论了你的动态', '这条转发很有共鸣，推荐和关注的比例确实要克制。', 30006, 50007, 0, NULL, DATE_SUB(NOW(), INTERVAL 4 MINUTE)),
  (85003, 10002, 'REPOST', 10003, 'Alice', 'Alice 转发了你的动态', '转发 @Life 用户： 转发 @Alice： 如果首页 feed 以关注内容为主，推荐内容就更像“打断式发现”，节奏一定要克制。', 30006, NULL, 0, NULL, DATE_SUB(NOW(), INTERVAL 3 MINUTE)),
  (85004, 10002, 'BROADCAST', NULL, 'Life 管理员', '欢迎来到 Life', '消息中心现已支持评论、点赞、转发的在线推送，管理后台也可以发送广播通知。', NULL, NULL, 0, NULL, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
  (85005, 10003, 'BROADCAST', NULL, 'Life 管理员', '欢迎来到 Life', '消息中心现已支持评论、点赞、转发的在线推送，管理后台也可以发送广播通知。', NULL, NULL, 0, NULL, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
  (85006, 10004, 'BROADCAST', NULL, 'Life 管理员', '欢迎来到 Life', '消息中心现已支持评论、点赞、转发的在线推送，管理后台也可以发送广播通知。', NULL, NULL, 0, NULL, DATE_SUB(NOW(), INTERVAL 2 MINUTE))
ON DUPLICATE KEY UPDATE
  `sender_name` = VALUES(`sender_name`),
  `title` = VALUES(`title`),
  `content_text` = VALUES(`content_text`),
  `post_id` = VALUES(`post_id`),
  `comment_id` = VALUES(`comment_id`),
  `is_read` = VALUES(`is_read`),
  `read_at` = VALUES(`read_at`),
  `created_at` = VALUES(`created_at`);
