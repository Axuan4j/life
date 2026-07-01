# 架构总览

## 技术栈
- 后端：Java 17、Spring Boot 3、Spring Security、MyBatis-Plus、MySQL 8、Redis。
- 管理端：Vue 3、Naive UI、Pinia、Vue Router、Axios。
- 用户 H5：Vue 3、Vant 4、Pinia、Vue Router、Axios。
- Android：Kotlin、Jetpack Compose、Retrofit、OkHttp、Room。

## 模块职责
- `life-boot`：应用启动、配置装配、统一暴露接口。
- `life-common`：返回体、异常体系、通用模型。
- `life-security`：JWT 与安全配置。
- `life-infra`：数据库、缓存、对象存储配置。
- `life-module-admin`：后台账号、RBAC、概览、广播消息。
- `life-module-user`：账户与个人主页。
- `life-module-content`：帖子及媒体。
- `life-module-social`：关注关系。
- `life-module-feed`：首页流、推荐策略、曝光记录。
- `life-module-message`：点赞/评论/转发/广播通知与 SSE 推送。

## 关键业务链路
- C 端登录：`/api/auth/captcha` -> `/api/auth/captcha/verify` -> `/api/auth/login`
- 首页 Feed：关注内容优先，规则推荐补位，统一使用 `cursor` 分页
- 发现页：热词、话题、推荐作者由后端配置和查询结果聚合提供
- 消息中心：通知列表 + 未读数 + SSE 在线推送
- 管理后台：动态菜单、权限守卫、角色菜单绑定、管理员角色分配

## 部署约定
- 后端按 `life-app.jar + lib/*.jar` 的薄包结构发布
- 生产固定从 `/etc/life/application-prod.yml` 读取外置配置
- 生产日志固定写入 `/opt/life/logs`

## Feed 设计
- V1 采用“关注流 + 规则推荐补位”的方案。
- 先读取关注作者的最新内容，再根据推荐策略补足指定条数。
- Feed 使用游标分页，避免页码流在内容持续写入时出现重复或漏读。

## 数据表建议
- `admin_account`
- `admin_role`
- `admin_menu`
- `admin_user_role`
- `admin_role_menu`
- `user_account`
- `user_profile`
- `post`
- `post_media`
- `post_stat`
- `post_comment`
- `post_like`
- `post_repost`
- `user_follow`
- `feed_exposure`
- `user_notification`
