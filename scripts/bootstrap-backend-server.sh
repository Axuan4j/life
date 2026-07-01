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
  bash scripts/bootstrap-backend-server.sh --host <server> [options]

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
remote_tmp="/tmp/life-bootstrap.$$"

echo "==> Preparing remote temp directory"
ssh -p "${DEPLOY_PORT}" "${ssh_target}" "rm -rf '${remote_tmp}' && mkdir -p '${remote_tmp}'"

echo "==> Uploading deployment assets"
scp -P "${DEPLOY_PORT}" \
  "${ROOT_DIR}/ops/deploy/deploy-life.sh" \
  "${ROOT_DIR}/ops/deploy/rollback-life.sh" \
  "${ROOT_DIR}/ops/deploy/life-app.env.example" \
  "${ROOT_DIR}/ops/deploy/application-prod.yml.example" \
  "${ROOT_DIR}/ops/systemd/life-app.service" \
  "${ssh_target}:${remote_tmp}/"

echo "==> Installing deployment assets on the server"
ssh -p "${DEPLOY_PORT}" "${ssh_target}" "REMOTE_TMP='${remote_tmp}' bash -s" <<'REMOTE'
set -euo pipefail

if [[ "$(id -u)" -ne 0 ]]; then
  echo "bootstrap must run as root on the server." >&2
  exit 1
fi

for cmd in getent groupadd grep install sed systemctl useradd id chown chmod; do
  if ! command -v "${cmd}" >/dev/null 2>&1; then
    echo "Missing required command on server: ${cmd}" >&2
    exit 1
  fi
done

if [[ ! -x /usr/bin/java ]]; then
  echo "/usr/bin/java is required by the systemd service template." >&2
  exit 1
fi

java_spec="$(/usr/bin/java -XshowSettings:properties -version 2>&1 | awk '/java.specification.version = / { print $3; exit }')"
if [[ "${java_spec}" != "17" ]]; then
  echo "Expected server Java 17 at /usr/bin/java, but found ${java_spec:-unknown}." >&2
  exit 1
fi

service_user="life"
service_group="life"
nologin_bin="$(command -v nologin || echo /usr/sbin/nologin)"

if ! getent group "${service_group}" >/dev/null 2>&1; then
  groupadd --system "${service_group}"
fi

if ! id -u "${service_user}" >/dev/null 2>&1; then
  useradd --system --gid "${service_group}" --home-dir /opt/life --shell "${nologin_bin}" "${service_user}"
fi

install -d -o root -g root -m 755 /opt/life
install -d -o root -g root -m 755 /opt/life/bin
install -d -o "${service_user}" -g "${service_group}" -m 755 /opt/life/releases
install -d -o "${service_user}" -g "${service_group}" -m 755 /opt/life/uploads
install -d -o "${service_user}" -g "${service_group}" -m 755 /opt/life/logs
install -d -o root -g "${service_group}" -m 750 /etc/life

install -o root -g root -m 755 "${REMOTE_TMP}/deploy-life.sh" /opt/life/bin/deploy-life.sh
install -o root -g root -m 755 "${REMOTE_TMP}/rollback-life.sh" /opt/life/bin/rollback-life.sh
install -o root -g root -m 644 "${REMOTE_TMP}/life-app.service" /etc/systemd/system/life-app.service
install -o root -g root -m 644 "${REMOTE_TMP}/life-app.env.example" /etc/life/life-app.env.example
install -o root -g root -m 644 "${REMOTE_TMP}/application-prod.yml.example" /etc/life/application-prod.yml.example

if [[ ! -f /etc/life/life-app.env ]]; then
  install -o root -g "${service_group}" -m 640 "${REMOTE_TMP}/life-app.env.example" /etc/life/life-app.env
fi

if [[ ! -f /etc/life/application-prod.yml ]]; then
  install -o root -g "${service_group}" -m 640 "${REMOTE_TMP}/application-prod.yml.example" /etc/life/application-prod.yml
fi

if grep -q '^LIFE_LOG_PATH=\./logs$' /etc/life/life-app.env; then
  sed -i 's#^LIFE_LOG_PATH=\./logs$#LIFE_LOG_PATH=/opt/life/logs#' /etc/life/life-app.env
fi

if ! grep -q '^LIFE_LOG_PATH=' /etc/life/life-app.env; then
  printf '\nLIFE_LOG_PATH=/opt/life/logs\n' >> /etc/life/life-app.env
fi

chmod 640 /etc/life/life-app.env
chown root:"${service_group}" /etc/life/life-app.env
chmod 640 /etc/life/application-prod.yml
chown root:"${service_group}" /etc/life/application-prod.yml

systemctl daemon-reload
systemctl enable life-app

rm -rf "${REMOTE_TMP}"
REMOTE

echo "==> Bootstrap finished"
echo "Edit /etc/life/life-app.env on the server before the first release if needed."
