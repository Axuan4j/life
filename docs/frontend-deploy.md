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
- 用户 H5 后端反代：`127.0.0.1:18080`
- 管理端后端反代：`127.0.0.1:18081`
- C 端目录：`/opt/life/web-user-h5`
- 管理端目录：`/opt/life/web-admin`

## 第一次初始化
```bash
bash scripts/bootstrap-web-server.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT>
```

## 发布 C 端
```bash
bash scripts/release-web-user-h5.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT>
```

## 发布管理端
```bash
bash scripts/release-web-admin.sh --host <SERVER_IP> --user <SSH_USER> --port <SSH_PORT>
```

## 说明
- 用户 H5 默认走站点同源 `/api/...`
- 只有确实要把 H5 API 指向其他地址时，才需要传 `--api-base-url`
- 管理端默认走同源 `/api/...`，不再要求按访问地址重新打包
- C 端默认构建成走当前站点自己的 `/api/...`
- 管理端的 `/api` 由站点自己的 `nginx` 反代到 `127.0.0.1:18081`
- 如果确实需要把管理端 API 指到别的地址，发布时可额外传 `--api-base-url http://<TARGET_HOST>:<TARGET_PORT>`
- `nginx` 会把用户 H5 的 `/api` 反代到 `127.0.0.1:18080`
- `nginx` 会把管理端的 `/api` 反代到 `127.0.0.1:18081`
- C 端 `notifications/stream` 单独关闭了代理缓冲，避免 SSE 被缓存
- C 端登录已经改成“点选验证码 -> `tempKey` -> 登录”的两段式流程，部署验证时建议直接在浏览器里走一次登录链路

更完整的命令版手册见：[deployment-guide.md](./deployment-guide.md)
