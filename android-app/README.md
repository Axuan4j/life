# Life Android

- 技术：Kotlin + Jetpack Compose + Retrofit + OkHttp + Room
- 当前能力：
  - 登录 / 注册页 UI 与本地 Token 持久化
  - 首页 Feed 拉取
  - 帖子详情、评论、点赞、转发、删除评论
  - 发帖页
  - 我的主页基础信息展示
  - 底部 Tab 框架：`首页 / 发现 / 发帖 / 消息 / 我的`
- 当前边界：
  - `发现`、`消息` 仍是占位页，还没有接入正式业务接口
  - Android 登录链路还没有同步 H5 的“点选验证码 -> tempKey -> 登录”安全流程，当前 README 只反映代码现状，不代表已经和最新后端认证规则完全对齐
