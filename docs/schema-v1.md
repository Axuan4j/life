# V1 数据模型建议

更完整的字段职责、索引原因和本地开发约定见 [database-design.md](/E:/CodeDevelop/Project/life/docs/database-design.md)。

## user_account
- `id` bigint 主键
- `username` varchar(64) 唯一
- `password_hash` varchar(255)
- `role_code` varchar(32)
- `status` tinyint
- `created_at` datetime
- `updated_at` datetime

## user_profile
- `id` bigint 主键
- `user_id` bigint 唯一
- `nickname` varchar(64)
- `avatar_url` varchar(255)
- `bio` varchar(255)
- `created_at` datetime
- `updated_at` datetime

## post
- `id` bigint 主键
- `author_id` bigint
- `content_text` varchar(2000)
- `visibility` varchar(32)
- `status` varchar(32)
- `published_at` datetime
- `created_at` datetime
- `updated_at` datetime

## post_media
- `id` bigint 主键
- `post_id` bigint
- `media_type` varchar(32)
- `media_url` varchar(255)
- `sort_order` int

## post_stat
- `post_id` bigint 主键
- `like_count` bigint
- `comment_count` bigint
- `repost_count` bigint

## user_follow
- `id` bigint 主键
- `follower_user_id` bigint
- `followed_user_id` bigint
- `created_at` datetime

## feed_exposure
- `id` bigint 主键
- `viewer_user_id` bigint
- `post_id` bigint
- `source_type` varchar(32)
- `cursor_token` varchar(128)
- `shown_at` datetime
