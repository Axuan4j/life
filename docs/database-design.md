# 数据库设计

## 设计目标
- 数据库优先服务 V1 核心闭环：登录、发帖、关注、首页 Feed、个人主页。
- 先保持模型简单、可解释、可扩展，不为未来的重功能提前引入过多表和复杂约束。
- 当前以 `MySQL 8` 为主库，`Redis` 负责缓存和轻量状态，媒体文件走 `MinIO`。

## 表与职责

### `user_account`
- 职责：用户登录账户、角色、状态。
- 关键字段：
  - `username`：登录名，唯一。
  - `password_hash`：BCrypt 哈希。
  - `role_code`：`USER` / `ADMIN`。
  - `status`：`1` 启用，`0` 禁用。
- 关键索引：
  - `uk_username`
- 设计说明：
  - 账户和资料分表，避免登录链路被用户展示字段拖重。

### `user_profile`
- 职责：用户公开资料与个人主页基础信息。
- 关键字段：
  - `user_id`：一对一关联 `user_account.id`
  - `nickname` / `avatar_url` / `bio`
- 关键索引：
  - `uk_user_profile_user_id`

### `post`
- 职责：帖子主表，承载发帖、个人主页、Feed 查询的核心数据。
- 关键字段：
  - `author_id`
  - `content_text`
  - `visibility`：当前先保留 `PUBLIC`
  - `status`：当前先保留 `PUBLISHED`
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
- 设计说明：
  - `sort_order` 不是多余字段，后续九宫格、视频封面、图文混排都依赖稳定顺序。

### `post_stat`
- 职责：帖子互动统计。
- 关键字段：
  - `like_count`
  - `comment_count`
  - `repost_count`
- 设计说明：
  - 单独拆表，避免主表频繁更新带来写热点放大。

### `user_follow`
- 职责：关注关系。
- 关键字段：
  - `follower_user_id`
  - `followed_user_id`
- 关键索引：
  - `uk_follow_pair`
  - `idx_follow_followed_user_id`
- 设计说明：
  - 关注关系是首页 Feed 的核心资产，唯一索引保证幂等关注。

### `feed_exposure`
- 职责：记录用户在首页看到过哪些帖子，用于调试、推荐分析、后续去重策略。
- 关键字段：
  - `viewer_user_id`
  - `post_id`
  - `source_type`
  - `cursor_token`
  - `shown_at`
- 关键索引：
  - `idx_feed_exposure_viewer_shown`
  - `idx_feed_exposure_post_id`
- 设计说明：
  - V1 先记录曝光，不把它做成复杂的强去重系统，避免首页链路过早复杂化。

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
- `feed_exposure.source_type`
  - `FOLLOWING`
  - `RECOMMENDED`

## 查询热点与索引策略
- 登录：`user_account.username`
- 个人主页：`post.author_id + published_at desc + id desc`
- 首页关注流：
  - 先查 `user_follow` 拿关注作者集合
  - 再按 `post.status + visibility + published_at + id` 拉取内容
- 曝光记录：
  - 按 `viewer_user_id + shown_at desc` 回看用户看过什么

## V1 暂不落表
- 点赞明细
- 评论明细
- 收藏明细
- 通知/消息
- 举报与审核工单
- 推荐画像/召回结果缓存

这些能力后续都能在当前模型上平滑扩展，不需要现在提前把数据库做重。
