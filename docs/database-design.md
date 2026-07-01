# 数据库设计

## 设计目标
- 数据库优先服务当前已落地的核心闭环：登录、发帖、评论、点赞、转发、关注、首页 Feed、发现页、消息中心、后台管理。
- 先保持模型简单、可解释、可扩展，不为未来的重功能提前引入过多表和复杂约束。
- 当前以 `MySQL 8` 为主库，`Redis` 负责缓存和轻量状态，媒体文件走 `MinIO`。
- 登录点选验证码挑战和一次性 `tempKey` 走 Redis，不落 MySQL。

## 账户与权限

### `user_account`
- 职责：C 端登录账户、角色、状态。
- 关键字段：
  - `username`：登录名，唯一
  - `password_hash`：BCrypt 哈希
  - `role_code`：`USER` / `ADMIN`
  - `status`：`1` 启用，`0` 禁用
  - `last_login_ip` / `last_login_region` / `last_login_at`
- 关键索引：
  - `uk_username`

### `user_profile`
- 职责：用户公开资料与个人主页基础信息。
- 关键字段：
  - `user_id`
  - `nickname`
  - `avatar_url`
  - `bio`
- 关键索引：
  - `uk_user_profile_user_id`

### `admin_account`
- 职责：后台管理员账户，与 C 端 `user_account` 完全分离。
- 关键字段：
  - `username`
  - `display_name`
  - `password_hash`
  - `status`
- 关键索引：
  - `uk_admin_account_username`

### `admin_role`
- 职责：后台角色定义。
- 关键字段：
  - `role_code`
  - `role_name`
  - `status`
  - `remark`
  - `is_system`
- 关键索引：
  - `uk_admin_role_code`

### `admin_menu`
- 职责：后台菜单、目录、页面和权限码定义。
- 关键字段：
  - `parent_id`
  - `menu_type`
  - `route_name`
  - `route_path`
  - `view_key`
  - `permission_code`
  - `sort_order`
  - `visible`
  - `status`
- 关键索引：
  - `uk_admin_menu_route_name`
  - `uk_admin_menu_permission_code`
  - `idx_admin_menu_parent_sort`

### `admin_user_role`
- 职责：管理员与角色绑定。
- 关键索引：
  - `uk_admin_user_role_pair`
  - `idx_admin_user_role_role_id`

### `admin_role_menu`
- 职责：角色与菜单绑定。
- 关键索引：
  - `uk_admin_role_menu_pair`
  - `idx_admin_role_menu_menu_id`

## 内容与互动

### `post`
- 职责：帖子主表，承载发帖、个人主页、Feed 查询的核心数据。
- 关键字段：
  - `author_id`
  - `content_text`
  - `visibility`
  - `status`
  - `client_ip`
  - `ip_region`
  - `published_at`
- 关键索引：
  - `idx_post_author_published`
  - `idx_post_status_visibility_published`
- 设计说明：
  - Feed 和主页都高度依赖 `published_at + id` 倒序查询，所以索引直接围绕这个访问路径设计。

### `post_media`
- 职责：帖子媒体资源。
- 关键字段：
  - `post_id`
  - `media_type`
  - `media_url`
  - `sort_order`
- 关键索引：
  - `idx_post_media_post_sort`

### `post_stat`
- 职责：帖子互动统计。
- 关键字段：
  - `like_count`
  - `comment_count`
  - `repost_count`
- 设计说明：
  - 单独拆表，避免主表频繁更新带来写热点放大。

### `post_comment`
- 职责：帖子评论与回复。
- 关键字段：
  - `post_id`
  - `user_id`
  - `parent_comment_id`
  - `reply_to_user_id`
  - `content_text`
  - `status`
  - `client_ip`
  - `ip_region`
- 关键索引：
  - `idx_post_comment_post_created`
  - `idx_post_comment_parent`
- 设计说明：
  - 当前评论区按“两级结构”渲染，但数据库层保留 parent/reply 字段，便于后续扩展。

### `post_like`
- 职责：点赞明细。
- 关键字段：
  - `post_id`
  - `user_id`
- 关键索引：
  - `uk_post_like_pair`
  - `idx_post_like_post_created`
- 设计说明：
  - 保留明细表，方便去重、点赞态判断和最近点赞用户展示。

### `post_repost`
- 职责：转发关系与转发生成的新帖子映射。
- 关键字段：
  - `post_id`
  - `user_id`
  - `repost_post_id`
- 关键索引：
  - `uk_post_repost_pair`
  - `idx_post_repost_post_created`
- 设计说明：
  - 同时表达“谁转发了哪条源帖”和“转发内容对应的新帖子”。

## 关系、推荐与消息

### `user_follow`
- 职责：关注关系。
- 关键字段：
  - `follower_user_id`
  - `followed_user_id`
- 关键索引：
  - `uk_follow_pair`
  - `idx_follow_followed_user_id`

### `feed_exposure`
- 职责：记录用户在首页看到过哪些帖子，用于调试、推荐分析和后续去重策略。
- 关键字段：
  - `viewer_user_id`
  - `post_id`
  - `source_type`
  - `cursor_token`
  - `shown_at`
- 关键索引：
  - `idx_feed_exposure_viewer_shown`
  - `idx_feed_exposure_post_id`

### `user_notification`
- 职责：点赞、评论、转发、广播等用户通知。
- 关键字段：
  - `receiver_user_id`
  - `notification_type`
  - `actor_user_id`
  - `sender_name`
  - `title`
  - `content_text`
  - `post_id`
  - `comment_id`
  - `is_read`
  - `read_at`
- 关键索引：
  - `idx_user_notification_receiver_read_created`
  - `idx_user_notification_receiver_created`
- 设计说明：
  - 列表读取、未读数统计和 SSE 推送都围绕“接收人 + 时间倒序”展开。

## 状态值约定
- `user_account.role_code`
  - `USER`
  - `ADMIN`
- `user_account.status`
  - `1`：启用
  - `0`：禁用
- `post.visibility`
  - `PUBLIC`
- `post.status`
  - `PUBLISHED`
- `post_comment.status`
  - `VISIBLE`
- `feed_exposure.source_type`
  - `FOLLOWING`
  - `RECOMMENDED`
- `user_notification.notification_type`
  - `LIKE`
  - `COMMENT`
  - `REPOST`
  - `BROADCAST`

## 查询热点与索引策略
- 登录：`user_account.username`
- 后台登录：`admin_account.username`
- 个人主页：`post.author_id + published_at desc + id desc`
- 首页关注流：
  - 先查 `user_follow` 拿关注作者集合
  - 再按 `post.status + visibility + published_at + id` 拉取内容
- 帖子详情互动：
  - 评论按 `post_id + created_at + id`
  - 点赞/转发明细按 `(post_id, user_id)` 幂等判断
- 消息中心：
  - `receiver_user_id + is_read + created_at desc`
- 曝光记录：
  - 按 `viewer_user_id + shown_at desc` 回看用户看过什么

## 当前未落地的典型能力
- 收藏
- 举报与审核工单
- 站内私信 / IM
- 搜索倒排索引
- 推荐召回结果缓存与画像表

这些能力后续都能在当前模型上平滑扩展，不需要现在提前把数据库做重。
