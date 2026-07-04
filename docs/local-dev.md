# 本地开发环境

## 服务组成
- `mysql`：主数据库，端口 `3306`
- `redis`：缓存，端口 `6379`
- `minio`：对象存储 API，端口 `9000`
- `minio-console`：MinIO 控制台，端口 `9001`

## 启动与停止
启动：
```bash
docker compose up -d
```

停止：
```bash
docker compose down
```

## 初始化内容
- `docker/mysql/init/001_full.sql`：Life 1.0 全量初始化 SQL，已包含建库、建表、索引、默认账号、后台菜单和开发联调用演示数据
- `minio-init`：自动创建 `life-dev` bucket

## 默认账号
- MySQL
  - `root / root`
  - `life / life123456`
- MinIO
  - `minioadmin / minioadmin`

## 默认种子账号
- 管理员
  - 用户名：`admin`
  - 密码：`admin123456`
- 普通用户
  - 用户名：`life_user`
  - 密码：`life123456`
- 演示博主
  - 用户名：`alice`
  - 密码：`life123456`
  - 用户名：`bob`
  - 密码：`life123456`

## 后端本地运行
- 后端默认 profile 是 `dev`
- 当前用户 H5 默认通过 Vite 的 `/api` 代理转发到“本机局域网 IP:18080”
- 这样即使本机 `127.0.0.1:18080` 被 SSH/终端工具本地转发占用，H5 本地联调也不会误打到隧道服务
- 当前管理端默认通过 Vite 的 `/api` 代理转发到 `http://localhost:18081`
- `application-dev.yml` 当前默认数据库地址是 `jdbc:mysql://localhost:14306/life`
- `application-dev.yml` 当前默认 Redis 密码是 `123456`，但仓库根目录的 `docker compose` 启动出来的 Redis 默认无密码
- 如果你直接使用仓库根目录的 `docker compose`，启动后端前建议显式覆盖为 `3306`：

```bash
export LIFE_DB_URL='jdbc:mysql://localhost:3306/life?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai'
export LIFE_DB_USERNAME='life'
export LIFE_DB_PASSWORD='life123456'
export LIFE_REDIS_HOST='localhost'
export LIFE_REDIS_PORT='6379'
export LIFE_REDIS_DATABASE='0'
export LIFE_REDIS_PASSWORD=''
export LIFE_STORAGE_ENDPOINT='http://localhost:9000'
export LIFE_STORAGE_ACCESS_KEY='minioadmin'
export LIFE_STORAGE_SECRET_KEY='minioadmin'
export LIFE_STORAGE_BUCKET='life-dev'
```

- 本地启动 C 端后端：

```bash
bash ./gradlew :backend:life-boot:bootRun
```

- 本地启动管理端后端：

```bash
bash ./gradlew :backend:life-admin-boot:bootRun
```

## 前端本地运行
用户 H5：
```bash
cd web-user-h5
pnpm install
pnpm dev
```

如果本地用户端后端不在 `18080`，或者你想手工指定代理目标，可以先临时指定：

```bash
cd web-user-h5
VITE_DEV_API_TARGET='http://127.0.0.1:18080' pnpm dev
```

管理端：
```bash
cd web-admin
pnpm install
pnpm dev
```

## 联调提示
- C 端登录现在是“点击登录 -> 弹出点选验证码 -> 验证通过后再提交账号密码”的链路
- 如果只想先验证后端是否可用，优先看健康检查和后台登录
- H5 登录链路建议直接在浏览器里走，不建议再按旧版“直接用户名密码 curl 登录”方式验证
