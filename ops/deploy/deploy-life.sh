#!/usr/bin/env bash

set -euo pipefail

SERVICE_NAME="${SERVICE_NAME:-life-app}"
APP_ROOT="${APP_ROOT:-/opt/life}"
APP_USER="${APP_USER:-life}"
APP_GROUP="${APP_GROUP:-life}"
CURRENT_LINK="${APP_ROOT}/current"
RELEASES_DIR="${APP_ROOT}/releases"
UPLOADS_DIR="${APP_ROOT}/uploads"
HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:18080/actuator/health}"
KEEP_RELEASES="${KEEP_RELEASES:-5}"
HEALTH_RETRIES="${HEALTH_RETRIES:-30}"
HEALTH_SLEEP_SECONDS="${HEALTH_SLEEP_SECONDS:-2}"
UPDATE_LIB="${UPDATE_LIB:-yes}"

usage() {
  cat <<'EOF'
Usage:
  /opt/life/bin/deploy-life.sh --app-jar <uploaded-jar-path> [options]

Options:
  --app-jar <path>        Uploaded application jar path. Required.
  --lib-archive <path>    Uploaded lib archive path. Required when --update-lib yes.
  --update-lib <yes|no>   Whether to replace server-side lib jars for this release. Default: yes
EOF
}

log() {
  printf '[deploy-life] %s\n' "$*"
}

require_root() {
  if [[ "$(id -u)" -ne 0 ]]; then
    echo "deploy-life.sh must run as root." >&2
    exit 1
  fi
}

require_commands() {
  local cmd
  for cmd in cp curl find install ln readlink rm sort systemctl tar; do
    if ! command -v "${cmd}" >/dev/null 2>&1; then
      echo "Missing required command: ${cmd}" >&2
      exit 1
    fi
  done
}

wait_for_health() {
  local attempt response
  for ((attempt = 1; attempt <= HEALTH_RETRIES; attempt++)); do
    if response="$(curl --silent --show-error --fail "${HEALTH_URL}" 2>/dev/null)" && [[ "${response}" == *'"status":"UP"'* ]]; then
      return 0
    fi
    sleep "${HEALTH_SLEEP_SECONDS}"
  done
  return 1
}

cleanup_old_releases() {
  local count=0 dir
  while IFS= read -r dir; do
    count=$((count + 1))
    if ((count > KEEP_RELEASES)); then
      rm -rf "${dir}"
      log "Removed old release ${dir}"
    fi
  done < <(find "${RELEASES_DIR}" -mindepth 1 -maxdepth 1 -type d | sort -r)
}

rollback_to_previous() {
  local failed_release_dir="$1"
  local previous_target="$2"

  if [[ -n "${previous_target}" && -d "${previous_target}" ]]; then
    ln -sfn "${previous_target}" "${CURRENT_LINK}"
    systemctl restart "${SERVICE_NAME}" || true
    if wait_for_health; then
      log "Rolled back to ${previous_target}"
    else
      log "Rollback restart did not become healthy"
    fi
  else
    rm -f "${CURRENT_LINK}"
    systemctl stop "${SERVICE_NAME}" || true
  fi

  rm -rf "${failed_release_dir}"
}

UPLOAD_JAR=""
UPLOAD_LIB_ARCHIVE=""

while (($# > 0)); do
  case "$1" in
    --app-jar)
      UPLOAD_JAR="${2:-}"
      shift 2
      ;;
    --lib-archive)
      UPLOAD_LIB_ARCHIVE="${2:-}"
      shift 2
      ;;
    --update-lib)
      UPDATE_LIB="${2:-}"
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

if [[ -z "${UPLOAD_JAR}" ]]; then
  echo "--app-jar is required." >&2
  usage
  exit 1
fi

if [[ "${UPDATE_LIB}" != "yes" && "${UPDATE_LIB}" != "no" ]]; then
  echo "--update-lib must be yes or no." >&2
  exit 1
fi

if [[ ! -f "${UPLOAD_JAR}" ]]; then
  echo "Uploaded jar not found: ${UPLOAD_JAR}" >&2
  exit 1
fi

if [[ "${UPDATE_LIB}" == "yes" && ! -f "${UPLOAD_LIB_ARCHIVE}" ]]; then
  echo "Uploaded lib archive not found: ${UPLOAD_LIB_ARCHIVE}" >&2
  exit 1
fi

require_root
require_commands

install -d -o "${APP_USER}" -g "${APP_GROUP}" -m 755 "${RELEASES_DIR}" "${UPLOADS_DIR}"

release_id="$(date '+%Y%m%d%H%M%S')"
release_dir="${RELEASES_DIR}/${release_id}"
previous_target=""

if [[ -L "${CURRENT_LINK}" ]]; then
  previous_target="$(readlink "${CURRENT_LINK}")"
fi

log "Creating release ${release_id}"
install -d -o "${APP_USER}" -g "${APP_GROUP}" -m 755 "${release_dir}"
install -o "${APP_USER}" -g "${APP_GROUP}" -m 644 "${UPLOAD_JAR}" "${release_dir}/life-app.jar"

if [[ "${UPDATE_LIB}" == "yes" ]]; then
  log "Extracting uploaded lib archive"
  install -d -o "${APP_USER}" -g "${APP_GROUP}" -m 755 "${release_dir}/lib"
  tar -xzf "${UPLOAD_LIB_ARCHIVE}" -C "${release_dir}/lib"
  chown -R "${APP_USER}:${APP_GROUP}" "${release_dir}/lib"
else
  if [[ -z "${previous_target}" || ! -d "${previous_target}/lib" ]]; then
    echo "Cannot reuse previous lib directory because no previous release lib exists." >&2
    rm -rf "${release_dir}"
    exit 1
  fi
  log "Reusing lib directory from ${previous_target}"
  install -d -o "${APP_USER}" -g "${APP_GROUP}" -m 755 "${release_dir}/lib"
  cp -al "${previous_target}/lib/." "${release_dir}/lib/"
fi

ln -sfn "${release_dir}" "${CURRENT_LINK}"

log "Restarting ${SERVICE_NAME}"
if ! systemctl restart "${SERVICE_NAME}"; then
  log "systemctl restart failed, rolling back"
  rollback_to_previous "${release_dir}" "${previous_target}"
  exit 1
fi

log "Waiting for health endpoint ${HEALTH_URL}"
if ! wait_for_health; then
  log "Health check failed, rolling back"
  rollback_to_previous "${release_dir}" "${previous_target}"
  exit 1
fi

rm -f "${UPLOAD_JAR}"
if [[ -n "${UPLOAD_LIB_ARCHIVE}" ]]; then
  rm -f "${UPLOAD_LIB_ARCHIVE}"
fi
cleanup_old_releases

log "Deployment succeeded"
