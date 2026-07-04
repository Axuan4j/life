# 部署手册

这是一份可直接分享的部署手册模板，所有 IP、端口、用户名、密码、密钥都使用占位符表示。

## 占位符说明
- `<SERVER_IP>`：服务器公网 IP
- `<SSH_PORT>`：服务器 SSH 端口
- `<SSH_USER>`：服务器登录用户
- `<USER_BACKEND_PORT>`：用户 C 端后端端口，建议 `18080`
- `<ADMIN_BACKEND_PORT>`：管理端后端端口，建议 `18081`
- `<WEB_H5_PORT>`：用户 H5 端口，建议 `9001`
- `<WEB_ADMIN_PORT>`：管理端前端端口，建议 `9002`
- `<DB_HOST>` `<DB_PORT>` `<DB_NAME>`：MySQL 连接信息
- `<DB_USERNAME>` `<DB_PASSWORD>`：MySQL 账号密码
- `<REDIS_HOST>` `<REDIS_PORT>` `<REDIS_PASSWORD>`：Redis 连接信息
- `<JWT_SECRET>`：JWT 密钥
- `<MINIO_ENDPOINT>` `<MINIO_ACCESS_KEY>` `<MINIO_SECRET_KEY>` `<MINIO_BUCKET>`：对象存储配置
- `<ADMIN_USERNAME>` `<ADMIN_PASSWORD>`：管理端登录账号

## 一、后端部署

### 1. 初始化服务器
在本地项目根目录执行：

```bash
bash scripts/bootstrap-backend-server.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT>
```

这一步会在服务器上安装：
- `/opt/life/bin/deploy-life.sh`
- `/opt/life/bin/rollback-life.sh`
- `/opt/life-admin/bin/deploy-life.sh`
- `/opt/life-admin/bin/rollback-life.sh`
- `/etc/systemd/system/life-app.service`
- `/etc/systemd/system/life-admin-app.service`
- `/etc/life/life-app.env`
- `/etc/life/life-admin-app.env`
- `/etc/life/application-prod.yml`
- `/etc/life/application-admin-prod.yml`

### 2. 配置用户 C 端后端
登录服务器：

```bash
ssh -p <SSH_PORT> <SSH_USER>@<SERVER_IP>
```

编辑文件：

```bash
vim /etc/life/life-app.env
vim /etc/life/application-prod.yml
```

`/etc/life/life-app.env` 示例：

```bash
SERVER_PORT=<USER_BACKEND_PORT>
LIFE_DB_URL=jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME>?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
LIFE_DB_USERNAME=<DB_USERNAME>
LIFE_DB_PASSWORD=<DB_PASSWORD>
LIFE_REDIS_HOST=<REDIS_HOST>
LIFE_REDIS_PORT=<REDIS_PORT>
LIFE_REDIS_DATABASE=0
LIFE_REDIS_PASSWORD=<REDIS_PASSWORD>
LIFE_LOG_PATH=/opt/life/logs
LIFE_LOG_MAX_HISTORY_DAYS=30
LIFE_JWT_ISSUER=life-user-app
LIFE_JWT_SECRET=<JWT_SECRET>
LIFE_STORAGE_ENDPOINT=<MINIO_ENDPOINT>
LIFE_STORAGE_ACCESS_KEY=<MINIO_ACCESS_KEY>
LIFE_STORAGE_SECRET_KEY=<MINIO_SECRET_KEY>
LIFE_STORAGE_BUCKET=<MINIO_BUCKET>
```

### 3. 配置管理端后端
编辑文件：

```bash
vim /etc/life/life-admin-app.env
vim /etc/life/application-admin-prod.yml
```

`/etc/life/life-admin-app.env` 示例：

```bash
SERVER_PORT=<ADMIN_BACKEND_PORT>
LIFE_DB_URL=jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME>?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
LIFE_DB_USERNAME=<DB_USERNAME>
LIFE_DB_PASSWORD=<DB_PASSWORD>
LIFE_REDIS_HOST=<REDIS_HOST>
LIFE_REDIS_PORT=<REDIS_PORT>
LIFE_REDIS_DATABASE=0
LIFE_REDIS_PASSWORD=<REDIS_PASSWORD>
LIFE_LOG_PATH=/opt/life-admin/logs
LIFE_LOG_MAX_HISTORY_DAYS=30
LIFE_JWT_ISSUER=life-admin-app
LIFE_JWT_SECRET=<JWT_SECRET>
LIFE_STORAGE_ENDPOINT=<MINIO_ENDPOINT>
LIFE_STORAGE_ACCESS_KEY=<MINIO_ACCESS_KEY>
LIFE_STORAGE_SECRET_KEY=<MINIO_SECRET_KEY>
LIFE_STORAGE_BUCKET=<MINIO_BUCKET>
```

### 4. 发布用户 C 端后端
```bash
bash scripts/release-backend.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT> \
  --health-url http://127.0.0.1:<USER_BACKEND_PORT>/actuator/health
```

如果依赖没变，只更新业务 jar：

```bash
bash scripts/release-backend.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT> \
  --health-url http://127.0.0.1:<USER_BACKEND_PORT>/actuator/health \
  --update-lib no
```

### 5. 发布管理端后端
```bash
bash scripts/release-admin-backend.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT> \
  --health-url http://127.0.0.1:<ADMIN_BACKEND_PORT>/actuator/health
```

如果依赖没变，只更新业务 jar：

```bash
bash scripts/release-admin-backend.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT> \
  --health-url http://127.0.0.1:<ADMIN_BACKEND_PORT>/actuator/health \
  --update-lib no
```

### 6. 查看后端状态
```bash
systemctl status life-app
systemctl status life-admin-app
journalctl -u life-app -f
journalctl -u life-admin-app -f
curl http://127.0.0.1:<USER_BACKEND_PORT>/actuator/health
curl http://127.0.0.1:<ADMIN_BACKEND_PORT>/actuator/health
```

### 7. 回滚
用户 C 端后端：

```bash
ssh -p <SSH_PORT> <SSH_USER>@<SERVER_IP> \
  "SERVICE_NAME=life-app APP_ROOT=/opt/life HEALTH_URL=http://127.0.0.1:<USER_BACKEND_PORT>/actuator/health /opt/life/bin/rollback-life.sh"
```

管理端后端：

```bash
ssh -p <SSH_PORT> <SSH_USER>@<SERVER_IP> \
  "SERVICE_NAME=life-admin-app APP_ROOT=/opt/life-admin HEALTH_URL=http://127.0.0.1:<ADMIN_BACKEND_PORT>/actuator/health /opt/life-admin/bin/rollback-life.sh"
```

## 二、前端部署

### 1. 初始化 Nginx 站点配置
```bash
bash scripts/bootstrap-web-server.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT>
```

这一步会安装：
- `/etc/nginx/vhost/life-web-user-h5.conf`
- `/etc/nginx/vhost/life-web-admin.conf`

当前约定：
- 用户 H5 `/api` 反代到 `127.0.0.1:<USER_BACKEND_PORT>`
- 管理端 `/api` 反代到 `127.0.0.1:<ADMIN_BACKEND_PORT>`

### 2. 发布用户 H5
```bash
bash scripts/release-web-user-h5.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT>
```

如果你确实要把用户 H5 API 指到一个独立地址，再额外带上：

```bash
bash scripts/release-web-user-h5.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT> \
  --api-base-url http://<TARGET_HOST>:<TARGET_PORT>
```

### 3. 发布管理端前端
```bash
bash scripts/release-web-admin.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT>
```

如果你确实要把管理端请求指向一个独立 API 地址，再额外带上：

```bash
bash scripts/release-web-admin.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT> \
  --api-base-url http://<TARGET_HOST>:<TARGET_PORT>
```

### 4. 验证站点
```bash
curl -I http://<SERVER_IP>:<WEB_H5_PORT>/
curl -I http://<SERVER_IP>:<WEB_ADMIN_PORT>/
curl http://127.0.0.1:<USER_BACKEND_PORT>/actuator/health
curl http://127.0.0.1:<ADMIN_BACKEND_PORT>/actuator/health
```

管理端登录验证：

```bash
cat <<'EOF' >/tmp/admin-login.json
{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}
EOF

curl -X POST \
  http://<SERVER_IP>:<WEB_ADMIN_PORT>/api/admin/auth/login \
  -H 'Content-Type: application/json' \
  --data @/tmp/admin-login.json

rm -f /tmp/admin-login.json
```

## 三、常用目录
- 用户 C 端后端当前版本：`/opt/life/current`
- 用户 C 端后端版本目录：`/opt/life/releases`
- 管理端后端当前版本：`/opt/life-admin/current`
- 管理端后端版本目录：`/opt/life-admin/releases`
- 用户 H5 当前版本：`/opt/life/web-user-h5/current`
- 管理端前端当前版本：`/opt/life/web-admin/current`

## 四、推荐发布顺序
1. 先确认 MySQL、Redis、对象存储可用
2. 先发布用户 C 端后端
3. 再发布管理端后端
4. 两个健康检查都通过后，再发前端
5. 最后做一次 H5 登录和管理端登录验证
