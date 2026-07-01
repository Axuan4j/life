# 前端发布说明

## 新增能力
- `scripts/bootstrap-web-server.sh`
  - 一次性安装前端静态部署脚本和 `nginx` 站点配置
- `scripts/release-web-user-h5.sh`
  - 构建并发布 C 端 H5 到服务器 `<WEB_H5_PORT>`
- `scripts/release-web-admin.sh`
  - 构建并发布管理端到服务器 `<WEB_ADMIN_PORT>`
- `ops/deploy/deploy-static-site.sh`
  - 服务器端静态站点版本切换脚本
- `ops/nginx/life-web-user-h5.conf`
  - C 端 H5 `nginx` 配置
- `ops/nginx/life-web-admin.conf`
  - 管理端 `nginx` 配置

## 固定约定
- C 端端口：`<WEB_H5_PORT>`
- 管理端端口：`<WEB_ADMIN_PORT>`
- 后端反代：`127.0.0.1:<BACKEND_PORT>`
- C 端目录：`/opt/life/web-user-h5`
- 管理端目录：`/opt/life/web-admin`

## 第一次初始化
```bash
bash scripts/bootstrap-web-server.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT>
```

## 发布 C 端
```bash
bash scripts/release-web-user-h5.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT> --public-origin http://<SERVER_IP>:<WEB_H5_PORT>
```

## 发布管理端
```bash
bash scripts/release-web-admin.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT> --public-origin http://<SERVER_IP>:<WEB_ADMIN_PORT>
```

## 说明
- 两个脚本都会在构建时注入站点自己的公网地址作为 `VITE_API_BASE_URL`
- C 端默认构建成走 `http://<SERVER_IP>:<WEB_H5_PORT>/api/...`
- 管理端默认构建成走 `http://<SERVER_IP>:<WEB_ADMIN_PORT>/api/...`
- `nginx` 负责把 `/api` 统一反代到本地后端 `127.0.0.1:<BACKEND_PORT>`
- C 端 `notifications/stream` 单独关闭了代理缓冲，避免 SSE 被缓存
- C 端登录已经改成“点选验证码 -> `tempKey` -> 登录”的两段式流程，部署验证时建议直接在浏览器里走一次登录链路

更完整的命令版手册见：[deployment-guide.md](./deployment-guide.md)
