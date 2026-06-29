# 架构总览

## 技术栈
- 后端：Java 17、Spring Boot 3、Spring Security、MyBatis-Plus、MySQL 8、Redis。
- 管理端：Vue 3、TDesign、Pinia、Vue Router、Axios。
- 用户 H5：Vue 3、Vant 4、Pinia、Vue Router、Axios。
- Android：Kotlin、Jetpack Compose、Retrofit、OkHttp、Room。

## 模块职责
- `life-boot`：应用启动、配置装配、统一暴露接口。
- `life-common`：返回体、异常体系、通用模型。
- `life-security`：JWT 与安全配置。
- `life-infra`：数据库、缓存、对象存储配置。
- `life-module-user`：账户与个人主页。
- `life-module-content`：帖子及媒体。
- `life-module-social`：关注关系。
- `life-module-feed`：首页流、推荐策略、曝光记录。

## Feed 设计
- V1 采用“关注流 + 规则推荐补位”的方案。
- 先读取关注作者的最新内容，再根据推荐策略补足指定条数。
- Feed 使用游标分页，避免页码流在内容持续写入时出现重复或漏读。

## 数据表建议
- `user_account`
- `user_profile`
- `post`
- `post_media`
- `post_stat`
- `user_follow`
- `feed_exposure`
