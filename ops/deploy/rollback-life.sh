#!/usr/bin/env bash

set -euo pipefail

SERVICE_NAME="${SERVICE_NAME:-life-app}"
APP_ROOT="${APP_ROOT:-/opt/life}"
CURRENT_LINK="${APP_ROOT}/current"
RELEASES_DIR="${APP_ROOT}/releases"
HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:18080/actuator/health}"
HEALTH_RETRIES="${HEALTH_RETRIES:-30}"
HEALTH_SLEEP_SECONDS="${HEALTH_SLEEP_SECONDS:-2}"

log() {
  printf '[rollback-life] %s\n' "$*"
}

require_commands() {
  local cmd
  for cmd in curl find ln readlink systemctl; do
    if ! command -v "${cmd}" >/dev/null 2>&1; then
      echo "Missing required command: ${cmd}" >&2
      exit 1
    fi
  done
}

require_root() {
  if [[ "$(id -u)" -ne 0 ]]; then
    echo "rollback-life.sh must run as root." >&2
    exit 1
  fi
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

require_root
require_commands

if [[ ! -L "${CURRENT_LINK}" ]]; then
  echo "Current release link does not exist: ${CURRENT_LINK}" >&2
  exit 1
fi

current_target="$(readlink "${CURRENT_LINK}")"
rollback_target=""

while IFS= read -r dir; do
  if [[ "${dir}" != "${current_target}" ]]; then
    rollback_target="${dir}"
    break
  fi
done < <(find "${RELEASES_DIR}" -mindepth 1 -maxdepth 1 -type d | sort -r)

if [[ -z "${rollback_target}" ]]; then
  echo "No previous release available for rollback." >&2
  exit 1
fi

log "Switching current release to ${rollback_target}"
ln -sfn "${rollback_target}" "${CURRENT_LINK}"

if ! systemctl restart "${SERVICE_NAME}"; then
  log "Restart failed, restoring ${current_target}"
  ln -sfn "${current_target}" "${CURRENT_LINK}"
  systemctl restart "${SERVICE_NAME}" || true
  exit 1
fi

if ! wait_for_health; then
  log "Health check failed after rollback, restoring ${current_target}"
  ln -sfn "${current_target}" "${CURRENT_LINK}"
  systemctl restart "${SERVICE_NAME}" || true
  exit 1
fi

log "Rollback succeeded"
