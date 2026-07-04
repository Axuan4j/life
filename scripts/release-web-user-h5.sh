#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
APP_DIR="${ROOT_DIR}/web-user-h5"
SITE_NAME="web-user-h5"

DEPLOY_HOST="${DEPLOY_HOST:-}"
DEPLOY_USER="${DEPLOY_USER:-root}"
DEPLOY_PORT="${DEPLOY_PORT:-22}"
KEEP_RELEASES="${KEEP_RELEASES:-5}"
API_BASE_URL="${API_BASE_URL:-}"
PUBLIC_ORIGIN="${PUBLIC_ORIGIN:-}"
BOOTSTRAP_FIRST=0

usage() {
  cat <<'EOF'
Usage:
  bash scripts/release-web-user-h5.sh --host <server> [options]

Options:
  --host <server>          Server host or IP.
  --user <user>            SSH user. Default: root
  --port <port>            SSH port. Default: 22
  --api-base-url <url>     Optional API base URL override. Default: same-origin /api
  --public-origin <url>    Backward-compatible alias for --api-base-url
  --keep-releases <count>  Keep the newest N releases on the server. Default: 5
  --bootstrap              Run bootstrap-web-server.sh before releasing.
  -h, --help               Show this help message.
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
    --api-base-url)
      API_BASE_URL="${2:-}"
      shift 2
      ;;
    --public-origin)
      PUBLIC_ORIGIN="${2:-}"
      shift 2
      ;;
    --keep-releases)
      KEEP_RELEASES="${2:-}"
      shift 2
      ;;
    --bootstrap)
      BOOTSTRAP_FIRST=1
      shift
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

if [[ -z "${API_BASE_URL}" && -n "${PUBLIC_ORIGIN}" ]]; then
  API_BASE_URL="${PUBLIC_ORIGIN}"
fi

run_bootstrap_if_requested() {
  if [[ "${BOOTSTRAP_FIRST}" != "1" ]]; then
    return
  fi
  bash "${SCRIPT_DIR}/bootstrap-web-server.sh" \
    --host "${DEPLOY_HOST}" \
    --user "${DEPLOY_USER}" \
    --port "${DEPLOY_PORT}"
}

build_site() {
  echo "==> Building ${SITE_NAME}"
  if [[ -n "${API_BASE_URL}" ]]; then
    CI=true VITE_API_BASE_URL="${API_BASE_URL}" pnpm -C "${APP_DIR}" build
    return
  fi

  CI=true pnpm -C "${APP_DIR}" build
}

package_site() {
  local archive_path="$1"
  COPYFILE_DISABLE=1 tar -C "${APP_DIR}/dist" -czf "${archive_path}" .
}

ensure_remote_ready() {
  local ssh_target="${DEPLOY_USER}@${DEPLOY_HOST}"
  ssh -p "${DEPLOY_PORT}" "${ssh_target}" "test -x /opt/life/bin/deploy-static-site.sh"
}

upload_and_deploy() {
  local archive_path="$1"
  local ssh_target="${DEPLOY_USER}@${DEPLOY_HOST}"
  local release_ts remote_upload_path upload_name

  release_ts="$(date '+%Y%m%d%H%M%S')"
  upload_name="${SITE_NAME}-${release_ts}.tar.gz"
  remote_upload_path="/opt/life/uploads/${upload_name}"

  echo "==> Uploading ${upload_name}"
  scp -P "${DEPLOY_PORT}" "${archive_path}" "${ssh_target}:${remote_upload_path}"

  echo "==> Triggering remote deploy"
  ssh -p "${DEPLOY_PORT}" "${ssh_target}" \
    "KEEP_RELEASES='${KEEP_RELEASES}' /opt/life/bin/deploy-static-site.sh '${SITE_NAME}' '${remote_upload_path}'"
}

run_bootstrap_if_requested
build_site

ARCHIVE_PATH="$(mktemp "/tmp/${SITE_NAME}.XXXXXX.tar.gz")"
trap 'rm -f "${ARCHIVE_PATH}"' EXIT
package_site "${ARCHIVE_PATH}"

echo "==> Verifying remote static deploy script"
if ! ensure_remote_ready; then
  echo "Remote static deploy script is missing. Run scripts/bootstrap-web-server.sh first or add --bootstrap." >&2
  exit 1
fi

upload_and_deploy "${ARCHIVE_PATH}"

echo "==> ${SITE_NAME} release finished"
