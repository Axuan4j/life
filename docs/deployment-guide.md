# 部署手册

这是一份可直接分享的部署手册模板，所有 IP、端口、用户名、密码、密钥都使用占位符表示。  
实际使用时，请把下文中的占位内容替换成你自己的真实配置。

## 占位符说明
- `<SERVER_IP>`：服务器公网 IP，例如 `203.0.113.10`
- `<SSH_PORT>`：服务器 SSH 端口，例如 `22`
- `<SSH_USER>`：服务器登录用户，例如 `root`
- `<BACKEND_PORT>`：Spring Boot 服务端口，例如 `18080`
- `<WEB_H5_PORT>`：C 端前端端口，例如 `9001`
- `<WEB_ADMIN_PORT>`：管理端前端端口，例如 `9002`
- `<DB_HOST>`：MySQL 主机，例如 `127.0.0.1`
- `<DB_PORT>`：MySQL 端口，例如 `3306`
- `<DB_NAME>`：数据库名，例如 `life`
- `<DB_USERNAME>`：数据库账号，例如 `life`
- `<DB_PASSWORD>`：数据库密码，例如 `replace_me`
- `<REDIS_HOST>`：Redis 主机，例如 `127.0.0.1`
- `<REDIS_PORT>`：Redis 端口，例如 `6379`
- `<REDIS_PASSWORD>`：Redis 密码，例如 `replace_me`
- `<JWT_SECRET>`：JWT 密钥，例如一段足够长的随机字符串
- `<MINIO_ENDPOINT>`：对象存储地址，例如 `http://127.0.0.1:9000`
- `<MINIO_ACCESS_KEY>`：对象存储账号
- `<MINIO_SECRET_KEY>`：对象存储密码
- `<MINIO_BUCKET>`：对象存储桶名
- `<ADMIN_USERNAME>`：后台管理员用户名，例如 `admin`
- `<ADMIN_PASSWORD>`：后台管理员密码，例如 `replace_me`

## 一、后端部署

### 1. 服务器初始化
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
- `/etc/systemd/system/life-app.service`
- `/etc/life/life-app.env`
- `/etc/life/application-prod.yml`

### 2. 配置后端环境变量
登录服务器：

```bash
ssh -p <SSH_PORT> <SSH_USER>@<SERVER_IP>
```

编辑环境文件：

```bash
vim /etc/life/life-app.env
vim /etc/life/application-prod.yml
```

`/etc/life/life-app.env` 示例内容：

```bash
SERVER_PORT=<BACKEND_PORT>

LIFE_DB_URL=jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME>?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
LIFE_DB_USERNAME=<DB_USERNAME>
LIFE_DB_PASSWORD=<DB_PASSWORD>

LIFE_REDIS_HOST=<REDIS_HOST>
LIFE_REDIS_PORT=<REDIS_PORT>
LIFE_REDIS_DATABASE=0
LIFE_REDIS_PASSWORD=<REDIS_PASSWORD>

LIFE_LOG_PATH=/opt/life/logs
LIFE_LOG_MAX_HISTORY_DAYS=30

LIFE_JWT_SECRET=<JWT_SECRET>

LIFE_STORAGE_ENDPOINT=<MINIO_ENDPOINT>
LIFE_STORAGE_ACCESS_KEY=<MINIO_ACCESS_KEY>
LIFE_STORAGE_SECRET_KEY=<MINIO_SECRET_KEY>
LIFE_STORAGE_BUCKET=<MINIO_BUCKET>
```

`/etc/life/application-prod.yml` 是生产外置配置主文件，后端 jar 内不再携带 `application.yml`。
服务器通过 `systemd` 固定启用 `prod` profile，并从 `/etc/life/` 读取这个文件。

保存后可检查权限：

```bash
ls -l /etc/life/life-app.env
```

### 3. 首次发布后端
回到本地项目根目录执行：

```bash
bash scripts/release-backend.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT> \
  --health-url http://127.0.0.1:<BACKEND_PORT>/actuator/health
```

如果这次后端没有依赖变化，只想更新应用 jar，可以加上：

```bash
bash scripts/release-backend.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT> \
  --health-url http://127.0.0.1:<BACKEND_PORT>/actuator/health \
  --update-lib no
```

如果服务器还没初始化，也可以一步完成：

```bash
bash scripts/release-backend.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT> \
  --health-url http://127.0.0.1:<BACKEND_PORT>/actuator/health \
  --bootstrap
```

说明：
- 新版后端发布会把 `life-app.jar` 和 `lib/*.jar` 分开部署
- 第一次切到这套结构前，务必先执行一次 `--bootstrap`，让服务器上的 `systemd` 服务模板更新为 `classpath` 启动方式
- 线上日志应该固定写到 `/opt/life/logs`，不要使用 `./logs` 这类相对路径
- 当前约定是：本地默认走 `application-dev.yml`，生产通过 `/etc/life/application-prod.yml` 提供外置配置

### 4. 查看后端运行状态
登录服务器后执行：

```bash
systemctl status life-app
journalctl -u life-app -f
readlink /opt/life/current
curl http://127.0.0.1:<BACKEND_PORT>/actuator/health
```

### 5. 后端回滚
登录服务器执行：

```bash
/opt/life/bin/rollback-life.sh
```

或者直接在本地执行：

```bash
ssh -p <SSH_PORT> <SSH_USER>@<SERVER_IP> /opt/life/bin/rollback-life.sh
```

## 二、前端部署

### 1. 初始化前端发布环境
在本地项目根目录执行：

```bash
bash scripts/bootstrap-web-server.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT>
```

这一步会在服务器上安装：
- `/opt/life/bin/deploy-static-site.sh`
- `/etc/nginx/vhost/life-web-user-h5.conf`
- `/etc/nginx/vhost/life-web-admin.conf`

并自动执行：

```bash
nginx -t
systemctl reload nginx
```

### 2. 发布 C 端 H5
在本地项目根目录执行：

```bash
bash scripts/release-web-user-h5.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT> \
  --public-origin http://<SERVER_IP>:<WEB_H5_PORT>
```

说明：
- 构建时会把 `VITE_API_BASE_URL` 注入成 `http://<SERVER_IP>:<WEB_H5_PORT>`
- 页面请求会走 `http://<SERVER_IP>:<WEB_H5_PORT>/api/...`
- `nginx` 会把 `/api` 反代到 `127.0.0.1:<BACKEND_PORT>`

### 3. 发布管理端
在本地项目根目录执行：

```bash
bash scripts/release-web-admin.sh \
  --host <SERVER_IP> \
  --user <SSH_USER> \
  --port <SSH_PORT> \
  --public-origin http://<SERVER_IP>:<WEB_ADMIN_PORT>
```

### 4. 检查前端站点状态
在本地验证：

```bash
curl -I http://<SERVER_IP>:<WEB_H5_PORT>/
curl -I http://<SERVER_IP>:<WEB_H5_PORT>/login

curl -I http://<SERVER_IP>:<WEB_ADMIN_PORT>/
curl -I http://<SERVER_IP>:<WEB_ADMIN_PORT>/login
```

用户 H5 登录验证：

```bash
open http://<SERVER_IP>:<WEB_H5_PORT>/login
```

说明：
- C 端登录现在是三段式：`/api/auth/captcha` -> `/api/auth/captcha/verify` -> `/api/auth/login`
- 因为验证码需要按图片内容完成点选，部署验收时推荐直接在浏览器完成一次真实登录
- 如果只做接口可用性校验，优先验证首页静态资源、后端健康检查和后台登录接口

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

### 5. 查看 Nginx 配置和重载状态
登录服务器执行：

```bash
nginx -t
systemctl status nginx
ls -l /etc/nginx/vhost/
sed -n '1,200p' /etc/nginx/vhost/life-web-user-h5.conf
sed -n '1,200p' /etc/nginx/vhost/life-web-admin.conf
```

如果手动修改了配置，重载命令是：

```bash
systemctl reload nginx
```

## 三、常用目录
- 后端当前版本：`/opt/life/current`
- 后端版本目录：`/opt/life/releases`
- 后端上传目录：`/opt/life/uploads`
- C 端当前版本：`/opt/life/web-user-h5/current`
- C 端版本目录：`/opt/life/web-user-h5/releases`
- 管理端当前版本：`/opt/life/web-admin/current`
- 管理端版本目录：`/opt/life/web-admin/releases`

## 四、常用排查命令

### 后端排查
```bash
systemctl status life-app --no-pager -l
journalctl -u life-app -n 200 --no-pager
ss -ltnp | grep <BACKEND_PORT>
curl http://127.0.0.1:<BACKEND_PORT>/actuator/health
```

### Nginx 排查
```bash
systemctl status nginx --no-pager -l
journalctl -u nginx -n 100 --no-pager
ss -ltnp | egrep ':<WEB_H5_PORT>|:<WEB_ADMIN_PORT>'
tail -n 100 /var/log/nginx/error.log
tail -n 100 /var/log/nginx/access.log
```

### 发布脚本排查
```bash
ls -lah /opt/life/bin/
ls -lah /opt/life/uploads/
ls -lah /opt/life/releases/
ls -lah /opt/life/web-user-h5/releases/
ls -lah /opt/life/web-admin/releases/
```

## 五、推荐发布顺序
1. 先确认数据库、Redis、对象存储都可用
2. 先发布后端
3. 确认后端健康检查通过
4. 再发布 C 端
5. 最后发布管理端
6. 用浏览器或 `curl` 做一次登录验证
