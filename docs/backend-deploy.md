# 后端发布说明

## 目标
- 本地构建 `backend:life-boot` 的应用 jar 和外置 `lib`
- 上传到服务器后按版本目录发布
- 使用 `systemd` 托管进程
- 支持快速回滚到上一版

## 新增文件
- `scripts/bootstrap-backend-server.sh`
  - 一次性初始化服务器目录、部署脚本、`systemd` 服务和环境变量模板
- `scripts/release-backend.sh`
  - 本地构建应用 jar 和 `lib`、上传到服务器、触发远端发布
- `ops/deploy/deploy-life.sh`
  - 服务器端部署脚本
- `ops/deploy/rollback-life.sh`
  - 服务器端回滚脚本
- `ops/deploy/life-app.env.example`
  - 生产环境变量模板
- `ops/deploy/application-prod.yml.example`
  - 生产外置配置模板
- `ops/systemd/life-app.service`
  - `systemd` 服务模板

## 固定约定
- 服务名：`life-app`
- 应用目录：`/opt/life`
- 当前版本：`/opt/life/current`
- 版本目录：`/opt/life/releases/<timestamp>`
- 上传目录：`/opt/life/uploads`
- 日志目录：`/opt/life/logs`
- 环境文件：`/etc/life/life-app.env`
- 生产配置文件：`/etc/life/application-prod.yml`
- 健康检查：`http://127.0.0.1:18080/actuator/health`

## 服务器前置条件
- Linux 服务器
- 使用 `root` 登录，或者具备同等权限
- 已安装 `systemd`
- `/usr/bin/java` 存在且为 Java 17
- 已经准备好 MySQL / Redis / MinIO

## 第一次初始化服务器
```bash
bash scripts/bootstrap-backend-server.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT>
```

初始化完成后，到服务器检查并修改环境变量：
```bash
ssh -p <SSH_PORT> <SSH_USER>@<SERVER_IP>
vim /etc/life/life-app.env
vim /etc/life/application-prod.yml
```

`/etc/life/life-app.env` 主要放端口、账号密码和密钥，可以从下面这组值开始调整：
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
LIFE_LOG_LEVEL=INFO
LIFE_APP_LOG_LEVEL=INFO
LIFE_SPRING_WEB_LOG_LEVEL=INFO
LIFE_MYBATIS_LOG_LEVEL=INFO
LIFE_JWT_SECRET=<JWT_SECRET>
LIFE_STORAGE_ENDPOINT=<MINIO_ENDPOINT>
LIFE_STORAGE_ACCESS_KEY=<MINIO_ACCESS_KEY>
LIFE_STORAGE_SECRET_KEY=<MINIO_SECRET_KEY>
LIFE_STORAGE_BUCKET=<MINIO_BUCKET>
```

`/etc/life/application-prod.yml` 是生产结构化配置主文件。生产发布时不再把 `application.yml` 打进 jar，而是固定读取这个外置文件。

## 日常发布
```bash
bash scripts/release-backend.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT>
```

如果这次只是业务代码变更，没有升级或新增依赖，可以只更新应用 jar，沿用上一版 `lib`：

```bash
bash scripts/release-backend.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT> --update-lib no
```

如果这台服务器还没做过初始化，也可以在第一次发布时带上：
```bash
bash scripts/release-backend.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT> --bootstrap
```

这个命令会依次执行：
1. 本地用 `bash ./gradlew :backend:life-boot:prepareReleaseLayout` 构建 `life-app.jar` 和 `lib`
2. 把应用 jar 上传到 `/opt/life/uploads`
3. 如果 `--update-lib yes`，再把 `lib` 打包后上传到 `/opt/life/uploads`
4. 调用 `/opt/life/bin/deploy-life.sh`
5. 切换 `/opt/life/current`
6. `systemctl restart life-app`
7. 检查 `/actuator/health`

线上日志固定写到 `/opt/life/logs`，不会跟着 `/opt/life/current` 切版本。
如果是早期已经初始化过的服务器，重新执行一次 `bash scripts/bootstrap-backend-server.sh ...`，它会补齐缺失的 `LIFE_LOG_PATH=/opt/life/logs`。
同一次 bootstrap 也会把 `/etc/life/application-prod.yml` 模板补到服务器上。

## 查看状态和日志
```bash
ssh -p <SSH_PORT> <SSH_USER>@<SERVER_IP>
systemctl status life-app
journalctl -u life-app -f
readlink /opt/life/current
ls -lh /opt/life/logs
```

## 手工回滚
```bash
ssh -p <SSH_PORT> <SSH_USER>@<SERVER_IP> /opt/life/bin/rollback-life.sh
```

更完整的命令版手册见：[deployment-guide.md](./deployment-guide.md)

回滚脚本会：
1. 找到当前版本之外最新的一个版本目录
2. 把 `/opt/life/current` 切回去
3. 重启 `life-app`
4. 再次检查健康接口

## 说明
- 发布脚本默认保留最近 `5` 个版本，可通过 `KEEP_RELEASES` 或 `--keep-releases` 调整
- 发布脚本默认 `--update-lib yes`
- 如果用了新的后端打包结构，第一次发布前需要先执行一次 `scripts/bootstrap-backend-server.sh`，把服务器上的 `deploy-life.sh` 和 `life-app.service` 更新到新版
- 当前约定是：本地默认走 `application-dev.yml`，生产通过 `systemd` 固定启用 `prod` profile，并从 `/etc/life/application-prod.yml` 读取外置配置
- `logback-spring.xml` 会同时输出控制台和文件日志，文件按天滚动，默认目录由 `LIFE_LOG_PATH` 控制
- 生产环境默认通过 `systemd` 兜底把 `LIFE_LOG_PATH` 固定成 `/opt/life/logs`，避免日志落到某个 release 目录里
- 当前实现默认服务进程使用 `life` 系统用户运行
- 服务器端不会执行 `git pull` 或 `gradle build`
- 如果健康检查失败，发布脚本会自动切回上一版，并删除这次失败的版本目录
