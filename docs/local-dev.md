# 本地开发环境

## 服务组成
- `mysql`：主数据库，端口 `3306`
- `redis`：缓存，端口 `6379`
- `minio`：对象存储 API，端口 `9000`
- `minio-console`：MinIO 控制台，端口 `9001`

## 启动
```bash
docker compose up -d
```

## 停止
```bash
docker compose down
```

## 初始化内容
- `docker/mysql/init/001_schema.sql`：建库建表与索引
- `docker/mysql/init/002_seed.sql`：开发种子数据
- `minio-init`：自动创建 `life-dev` bucket

## 默认账号
- MySQL
  - `root / root`
  - `life / life123456`
- MinIO
  - `minioadmin / minioadmin`

## 默认种子用户
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

## 后端本地运行建议
- 当前后端默认连本机：
  - MySQL：`jdbc:mysql://localhost:3306/life`
  - Redis：`localhost:6379`
- 如需启用 MinIO 上传，启动后端前设置：
```powershell
$env:LIFE_STORAGE_ENDPOINT="http://localhost:9000"
$env:LIFE_STORAGE_ACCESS_KEY="minioadmin"
$env:LIFE_STORAGE_SECRET_KEY="minioadmin"
$env:LIFE_STORAGE_BUCKET="life-dev"
```
