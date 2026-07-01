#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

DEPLOY_HOST="${DEPLOY_HOST:-}"
DEPLOY_USER="${DEPLOY_USER:-root}"
DEPLOY_PORT="${DEPLOY_PORT:-22}"

usage() {
  cat <<'EOF'
Usage:
  bash scripts/bootstrap-web-server.sh --host <server> [options]

Options:
  --host <server>  Server host or IP.
  --user <user>    SSH user. Default: root
  --port <port>    SSH port. Default: 22
  -h, --help       Show this help message.
EOF
}

while (($# > 0)); do
  case "$1" in
    --host)
      DEPLOY_HOST="${2:-}"
      shift 2
      ;;
    --user)
      DEPLOY_USER="${2:-}"
      shift 2
      ;;
    --port)
      DEPLOY_PORT="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ -z "${DEPLOY_HOST}" ]]; then
  echo "--host is required." >&2
  usage
  exit 1
fi

ssh_target="${DEPLOY_USER}@${DEPLOY_HOST}"
remote_tmp="/tmp/life-web-bootstrap.$$"

echo "==> Preparing remote temp directory"
ssh -p "${DEPLOY_PORT}" "${ssh_target}" "rm -rf '${remote_tmp}' && mkdir -p '${remote_tmp}'"

echo "==> Uploading web deployment assets"
scp -P "${DEPLOY_PORT}" \
  "${ROOT_DIR}/ops/deploy/deploy-static-site.sh" \
  "${ROOT_DIR}/ops/nginx/life-web-user-h5.conf" \
  "${ROOT_DIR}/ops/nginx/life-web-admin.conf" \
  "${ssh_target}:${remote_tmp}/"

echo "==> Installing web deployment assets on the server"
ssh -p "${DEPLOY_PORT}" "${ssh_target}" "REMOTE_TMP='${remote_tmp}' bash -s" <<'REMOTE'
set -euo pipefail

if [[ "$(id -u)" -ne 0 ]]; then
  echo "bootstrap-web must run as root on the server." >&2
  exit 1
fi

for cmd in install nginx systemctl tar; do
  if ! command -v "${cmd}" >/dev/null 2>&1; then
    echo "Missing required command on server: ${cmd}" >&2
    exit 1
  fi
done

install -d -o root -g root -m 755 /opt/life
install -d -o root -g root -m 755 /opt/life/bin
install -d -o root -g root -m 755 /opt/life/uploads
install -d -o root -g root -m 755 /opt/life/web-user-h5/releases
install -d -o root -g root -m 755 /opt/life/web-admin/releases

install -o root -g root -m 755 "${REMOTE_TMP}/deploy-static-site.sh" /opt/life/bin/deploy-static-site.sh
install -o root -g root -m 644 "${REMOTE_TMP}/life-web-user-h5.conf" /etc/nginx/vhost/life-web-user-h5.conf
install -o root -g root -m 644 "${REMOTE_TMP}/life-web-admin.conf" /etc/nginx/vhost/life-web-admin.conf

nginx -t
systemctl reload nginx

rm -rf "${REMOTE_TMP}"
REMOTE

echo "==> Web bootstrap finished"
