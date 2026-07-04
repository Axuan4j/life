# 后端发布说明

## 目标
- 用户 C 端后端和管理端后端分别独立发布
- 两套应用都使用 `jar + lib` 薄包结构
- 两套应用都使用 `systemd` 托管
- 两套应用都支持版本目录切换和快速回滚

## 新增与现有文件
- `scripts/bootstrap-backend-server.sh`
  - 一次性初始化用户端和管理端两套后端发布环境
- `scripts/release-backend.sh`
  - 发布用户 C 端后端
- `scripts/release-admin-backend.sh`
  - 发布管理端后端
- `scripts/release-backend-common.sh`
  - 后端发布共享逻辑
- `ops/deploy/deploy-life.sh`
  - 服务器端通用部署脚本
- `ops/deploy/rollback-life.sh`
  - 服务器端通用回滚脚本
- `ops/systemd/life-app.service`
  - 用户 C 端后端服务
- `ops/systemd/life-admin-app.service`
  - 管理端后端服务
- `ops/deploy/life-app.env.example`
  - 用户 C 端后端环境变量模板
- `ops/deploy/life-admin-app.env.example`
  - 管理端后端环境变量模板
- `ops/deploy/application-prod.yml.example`
  - 用户 C 端后端外置配置模板
- `ops/deploy/application-admin-prod.yml.example`
  - 管理端后端外置配置模板

## 固定约定

### 用户 C 端后端
- 服务名：`life-app`
- 应用目录：`/opt/life`
- 当前版本：`/opt/life/current`
- 环境文件：`/etc/life/life-app.env`
- 外置配置：`/etc/life/application-prod.yml`
- 健康检查：`http://127.0.0.1:18080/actuator/health`
- 默认 jar 名：`life-app.jar`

### 管理端后端
- 服务名：`life-admin-app`
- 应用目录：`/opt/life-admin`
- 当前版本：`/opt/life-admin/current`
- 环境文件：`/etc/life/life-admin-app.env`
- 外置配置：`/etc/life/application-admin-prod.yml`
- 健康检查：`http://127.0.0.1:18081/actuator/health`
- 默认 jar 名：`life-admin-app.jar`

## 第一次初始化服务器
```bash
bash scripts/bootstrap-backend-server.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT>
```

初始化后，服务器会多出这两套服务和配置：
- `/etc/systemd/system/life-app.service`
- `/etc/systemd/system/life-admin-app.service`
- `/etc/life/life-app.env`
- `/etc/life/life-admin-app.env`
- `/etc/life/application-prod.yml`
- `/etc/life/application-admin-prod.yml`

## 初始化后要修改的文件
```bash
ssh -p <SSH_PORT> <SSH_USER>@<SERVER_IP>
vim /etc/life/life-app.env
vim /etc/life/application-prod.yml
vim /etc/life/life-admin-app.env
vim /etc/life/application-admin-prod.yml
```

### 用户 C 端后端环境变量示例
```bash
SERVER_PORT=18080
LIFE_DB_URL=jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME>?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
LIFE_DB_USERNAME=<DB_USERNAME>
LIFE_DB_PASSWORD=<DB_PASSWORD>
LIFE_REDIS_HOST=<REDIS_HOST>
LIFE_REDIS_PORT=<REDIS_PORT>
LIFE_REDIS_DATABASE=0
LIFE_REDIS_PASSWORD=<REDIS_PASSWORD>
LIFE_LOG_PATH=/opt/life/logs
LIFE_JWT_ISSUER=life-user-app
LIFE_JWT_SECRET=<JWT_SECRET>
LIFE_STORAGE_ENDPOINT=<MINIO_ENDPOINT>
LIFE_STORAGE_ACCESS_KEY=<MINIO_ACCESS_KEY>
LIFE_STORAGE_SECRET_KEY=<MINIO_SECRET_KEY>
LIFE_STORAGE_BUCKET=<MINIO_BUCKET>
```

### 管理端后端环境变量示例
```bash
SERVER_PORT=18081
LIFE_DB_URL=jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME>?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
LIFE_DB_USERNAME=<DB_USERNAME>
LIFE_DB_PASSWORD=<DB_PASSWORD>
LIFE_REDIS_HOST=<REDIS_HOST>
LIFE_REDIS_PORT=<REDIS_PORT>
LIFE_REDIS_DATABASE=0
LIFE_REDIS_PASSWORD=<REDIS_PASSWORD>
LIFE_LOG_PATH=/opt/life-admin/logs
LIFE_JWT_ISSUER=life-admin-app
LIFE_JWT_SECRET=<JWT_SECRET>
LIFE_STORAGE_ENDPOINT=<MINIO_ENDPOINT>
LIFE_STORAGE_ACCESS_KEY=<MINIO_ACCESS_KEY>
LIFE_STORAGE_SECRET_KEY=<MINIO_SECRET_KEY>
LIFE_STORAGE_BUCKET=<MINIO_BUCKET>
```

## 日常发布

### 发布用户 C 端后端
```bash
bash scripts/release-backend.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT>
```

只更 jar、不更新依赖：
```bash
bash scripts/release-backend.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT> --update-lib no
```

### 发布管理端后端
```bash
bash scripts/release-admin-backend.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT>
```

只更 jar、不更新依赖：
```bash
bash scripts/release-admin-backend.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT> --update-lib no
```

### 第一次发布时顺手 bootstrap
```bash
bash scripts/release-backend.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT> --bootstrap
bash scripts/release-admin-backend.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT> --bootstrap
```

## 发布链路

### 用户 C 端后端
1. 本地执行 `bash ./gradlew :backend:life-boot:prepareReleaseLayout`
2. 上传 `life-app.jar`
3. 视情况上传 `lib` 压缩包
4. 调用 `/opt/life/bin/deploy-life.sh`
5. 重启 `life-app`
6. 检查 `http://127.0.0.1:18080/actuator/health`

### 管理端后端
1. 本地执行 `bash ./gradlew :backend:life-admin-boot:prepareReleaseLayout`
2. 上传 `life-admin-app.jar`
3. 视情况上传 `lib` 压缩包
4. 调用 `/opt/life-admin/bin/deploy-life.sh`
5. 重启 `life-admin-app`
6. 检查 `http://127.0.0.1:18081/actuator/health`

## 查看状态和日志
```bash
ssh -p <SSH_PORT> <SSH_USER>@<SERVER_IP>
systemctl status life-app
systemctl status life-admin-app
journalctl -u life-app -f
journalctl -u life-admin-app -f
readlink /opt/life/current
readlink /opt/life-admin/current
ls -lh /opt/life/logs
ls -lh /opt/life-admin/logs
```

## 手工回滚

### 用户 C 端后端
```bash
ssh -p <SSH_PORT> <SSH_USER>@<SERVER_IP> "SERVICE_NAME=life-app APP_ROOT=/opt/life HEALTH_URL=http://127.0.0.1:18080/actuator/health /opt/life/bin/rollback-life.sh"
```

### 管理端后端
```bash
ssh -p <SSH_PORT> <SSH_USER>@<SERVER_IP> "SERVICE_NAME=life-admin-app APP_ROOT=/opt/life-admin HEALTH_URL=http://127.0.0.1:18081/actuator/health /opt/life-admin/bin/rollback-life.sh"
```

## 说明
- 两套后端现在是真正独立的 Spring Boot Application
- 两套 JWT `issuer` 默认不同，避免 token 串用
- 线上日志分别固定写到 `/opt/life/logs` 和 `/opt/life-admin/logs`
- 两套服务都从 `/etc/life/` 读取外置 prod 配置，不依赖 jar 内开发配置
- 如果健康检查失败，发布脚本会自动切回上一版
- 更完整的占位符版命令手册见 [deployment-guide.md](./deployment-guide.md)
