# 用户端移动母版设计稿 V1

![用户端移动母版设计稿 V1](/E:/CodeDevelop/Project/life/docs/assets/user-mobile-design-v1.png)

## 目标
- 这是一套供 `Android` 与 `H5` 共用的移动端视觉母版。
- V1 先覆盖核心闭环：`登录`、`首页 Feed`、`发帖`、`个人主页`。
- 设计重点是统一视觉方向、组件语言和信息层级，避免两端各自发展。

## 视觉方向
- 关键词：`内容社区感`、`轻社交`、`内容优先`、`清爽但不平淡`。
- 风格原则：
  - 首页优先服务内容浏览，而不是品牌炫技。
  - 用暖橙红建立品牌记忆点，用蓝色和绿色承担辅助状态。
  - 以浅色分层背景、圆角卡片、轻阴影营造轻盈界面，不走纯白平铺。

## Design Tokens

### 色彩
- `Primary / 主品牌色`：`#FF5A3C`
- `Primary Strong / 主强调色`：`#FF4A26`
- `Secondary / 辅助蓝`：`#2D7FF9`
- `Accent / 正向绿`：`#22C55E`
- `Background / 页面背景`：`#F7F8FA`
- `Surface / 卡片背景`：`#FFFFFF`
- `Text Primary / 主文本`：`#1F2937`
- `Text Secondary / 次文本`：`#6B7280`
- `Divider / 分割线`：`#E5E7EB`
- `Warm Gradient Start`：`#FFF4EE`
- `Warm Gradient End`：`#F8FBFF`

### 字体层级
- `标题大 / H1`：`20px`，`SemiBold`
- `标题中 / H2`：`18px`，`Medium`
- `正文 / Body`：`15px`，`Regular`
- `说明 / Caption`：`12px`，`Regular`
- `标签 / Badge`：`12px`，`Medium`
- `统计 / Meta`：`13px`，`Regular`

### 间距
- `4 / 8 / 12 / 16 / 20 / 24 / 32 / 40`
- 规则：
  - 页面基础内边距：`16`
  - 卡片内边距：`16-20`
  - 模块间距：`16`
  - 大模块间距：`24-32`

### 圆角
- `8`：小输入项
- `12`：按钮、轻量标签容器
- `16`：普通卡片
- `24`：品牌卡片 / 大容器

### 阴影
- `Card Shadow`：轻阴影，强调内容浮层感，不做夸张悬浮
- `Hero Shadow`：只用于品牌头图和主资料卡

## 核心组件

### 底部导航
- 四项：`首页`、`消息`、`发帖`、`我的`
- 中间 `发帖` 可作为主强调操作
- 选中态使用主品牌色，未选中态保持中性灰
- Android 和 H5 都保留底部导航固定结构

### Feed 卡片
- 结构：
  - 作者信息
  - 时间 / 辅助描述
  - 内容来源标签：`关注` / `推荐`
  - 正文
  - 互动统计
- 推荐与关注只做轻量颜色区分，不改变整体卡片结构

### 品牌头卡
- 用于登录页、首页顶部、发帖页顶部
- 承担品牌语义与页面定位
- 允许使用暖色渐变，但不能压过内容信息

### 主按钮 / 次按钮
- 主按钮：暖橙红纯色，圆角 `12`
- 次按钮：白底或浅底描边
- 一页只保留一个最强主操作

### 输入区
- 保持大面积留白
- 文本输入优先，媒体入口做轻量附加
- 不在第一版堆复杂筛选、权限和设置项

### 资料卡
- 头像、昵称、简介、统计信息为第一层
- 我的帖子、资料编辑、设置入口为第二层
- 保持卡片式组织，和首页统一语言

## 页面定义

### 登录页
- 顶部：品牌标识 + 轻插画/氛围区
- 中部：简洁表单卡
- 底部：主登录按钮、次级登录方式入口
- 不做后台式重表单，不出现复杂说明文案

### 首页 Feed
- 顶部：简洁品牌头
- 主体：单列内容流
- 内容卡片节奏均匀，首屏优先展示 2-3 条高质量卡片结构
- Feed 是全端统一视觉的核心页面

### 发帖页
- 顶部：页面标题 + 发布动作
- 中部：正文输入主区域
- 下方：媒体占位、话题、位置、可见性入口
- 第一版强调“可写、可发、清楚”，不强调复杂运营配置

### 个人主页
- 顶部：资料卡
- 中部：关注 / 粉丝 / 获赞统计
- 下方：我的帖子、资料编辑、设置等入口
- 与首页卡片语言统一，避免做成“另一套 App”

## 多端适配规则
- Android 与 H5 共用：
  - 色彩
  - 间距
  - 组件结构
  - 信息层级
- Android 可更贴近系统安全区与手势区
- H5 可更贴近浏览器安全区与 Tabbar 习惯
- 不允许出现同一页面在 Android / H5 上完全不同的视觉骨架

## 开发映射
- `登录页`
  - H5: [LoginView.vue](/E:/CodeDevelop/Project/life/web-user-h5/src/views/LoginView.vue)
  - Android: 后续新增登录页时直接复用本规范
- `首页 Feed`
  - H5: [FeedHomeView.vue](/E:/CodeDevelop/Project/life/web-user-h5/src/views/FeedHomeView.vue)
  - Android: [HomeScreen.kt](/E:/CodeDevelop/Project/life/android-app/app/src/main/java/com/xuan/life/android/ui/screen/HomeScreen.kt)
- `发帖`
  - H5: [PostComposerView.vue](/E:/CodeDevelop/Project/life/web-user-h5/src/views/PostComposerView.vue)
  - Android: [ComposeScreen.kt](/E:/CodeDevelop/Project/life/android-app/app/src/main/java/com/xuan/life/android/ui/screen/ComposeScreen.kt)
- `我的`
  - H5: [ProfileView.vue](/E:/CodeDevelop/Project/life/web-user-h5/src/views/ProfileView.vue)
  - Android: [ProfileScreen.kt](/E:/CodeDevelop/Project/life/android-app/app/src/main/java/com/xuan/life/android/ui/screen/ProfileScreen.kt)

## 设计稿使用说明
- 先把这份母版当作 `统一参考标准`，再去改 Android 和 H5 的现有页面。
- 后续如果扩展消息页、空状态、详情页，必须沿用同一套 token 和卡片语言。
- 如果某一端因为组件库限制需要妥协，只允许改实现，不允许改视觉原则。
