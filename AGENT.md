# Life 项目全局方向

## 项目定位
- 这是一个类似微博的社交内容平台单仓工程，优先完成 V1 核心闭环，而不是一开始追求大而全。
- 当前核心闭环已经覆盖：`登录`、`发帖`、`评论`、`点赞`、`转发`、`关注/取关`、`首页 Feed`、`发现页`、`消息中心`、`个人主页`、`后台管理`。

## 仓库结构
- `backend/`：Java 17 + Spring Boot 单体后端，多模块拆分，统一部署。
- `web-admin/`：Vue 3 + Naive UI 管理端，负责概览、用户、帖子、 RBAC、广播消息等后台能力。
- `web-user-h5/`：Vue 3 + Vant 用户移动端 H5，负责点选验证码登录、内容消费、发现、消息和互动入口。
- `android-app/`：Kotlin + Jetpack Compose Android 端，第二阶段落地。
- `docs/`：架构文档、数据模型、接口约定、产品规则。

## 后端架构原则
- 保持单体部署，但按业务边界拆模块，避免把单体写成“大泥球”。
- 推荐分层：`controller -> service -> domain/entity -> mapper`，通用能力沉淀到 `life-common` / `life-infra`。
- DTO / VO / Entity 必须分离，禁止直接暴露数据库实体给前端。
- 核心业务模块：
  - `life-module-admin`：后台账号、菜单、角色、权限、广播消息。
  - `life-security`：JWT、安全过滤链、角色隔离。
  - `life-module-user`：用户账户、资料、主页。
  - `life-module-content`：帖子、媒体、统计。
  - `life-module-social`：关注关系。
  - `life-module-feed`：首页 Feed、规则推荐、曝光记录。
  - `life-module-message`：通知、广播消息、SSE 推送。

## API 与业务规则
- 统一返回体：`code`、`message`、`data`、`requestId`。
- 普通列表可用页码分页，Feed 流必须使用 `cursor` 分页，返回 `nextCursor` 和 `hasMore`。
- C 端登录采用两段式校验：先取点选验证码，再校验验证码换 `tempKey`，最后携带 `tempKey` 完成登录。
- API 前缀约定：
  - `/api/auth/*`
  - `/api/users/*`
  - `/api/posts/*`
  - `/api/follows/*`
  - `/api/feed/*`
  - `/api/discover/*`
  - `/api/notifications/*`
  - `/api/admin/*`
- 首页 Feed 规则：
  - 以关注用户最近内容为主。
  - 关注内容不足时，用规则推荐补足。
  - 推荐逻辑先追求“简单、可解释、可替换”，不提前引入复杂模型。

## 代码规范
- 关键部分必须带有注释，尤其是：
  - Security 过滤链与权限分流。
  - Feed 混排逻辑与 cursor 设计。
  - 推荐打分规则。
  - 复杂 SQL、缓存策略、自定义 MyBatis-Plus 查询。
- 命名优先表达业务语义，不做晦涩缩写。
- 复杂流程优先写清“为什么这样做”，而不是只写“代码做了什么”。

## 开发顺序
1. 搭建多模块根工程和基础脚手架。
2. 完成后端公共能力、鉴权、用户/帖子/关注/Feed。
3. 完成管理端基础后台。
4. 完成用户 H5 核心闭环。
5. 完成 Android 端一期实现。

## 非目标
- V1 不上微服务。
- V1 不做复杂推荐模型、消息队列、全文检索、直播、IM。
- V1 不接入真实短信、复杂风控或第三方登录，只预留扩展点。
