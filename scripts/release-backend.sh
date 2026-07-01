#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

DEPLOY_HOST="${DEPLOY_HOST:-}"
DEPLOY_USER="${DEPLOY_USER:-root}"
DEPLOY_PORT="${DEPLOY_PORT:-22}"
KEEP_RELEASES="${KEEP_RELEASES:-5}"
HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:18080/actuator/health}"
UPDATE_LIB="${UPDATE_LIB:-yes}"
BOOTSTRAP_FIRST=0

usage() {
  cat <<'EOF'
Usage:
  bash scripts/release-backend.sh --host <server> [options]

Options:
  --host <server>          Server host or IP.
  --user <user>            SSH user. Default: root
  --port <port>            SSH port. Default: 22
  --keep-releases <count>  Keep the newest N releases on the server. Default: 5
  --health-url <url>       Health check URL on the server. Default: http://127.0.0.1:18080/actuator/health
  --update-lib <yes|no>    Whether to upload and replace lib jars. Default: yes
  --bootstrap              Run bootstrap-backend-server.sh before releasing.
  -h, --help               Show this help message.

Environment variables:
  DEPLOY_HOST
  DEPLOY_USER
  DEPLOY_PORT
  KEEP_RELEASES
  HEALTH_URL
  UPDATE_LIB
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
    --keep-releases)
      KEEP_RELEASES="${2:-}"
      shift 2
      ;;
    --health-url)
      HEALTH_URL="${2:-}"
      shift 2
      ;;
    --update-lib)
      UPDATE_LIB="${2:-}"
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

if [[ "${UPDATE_LIB}" != "yes" && "${UPDATE_LIB}" != "no" ]]; then
  echo "--update-lib must be yes or no." >&2
  exit 1
fi

ensure_local_java17() {
  if [[ "$(uname -s)" == "Darwin" ]] && [[ -x /usr/libexec/java_home ]]; then
    export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17)}"
    export PATH="${JAVA_HOME}/bin:${PATH}"
  fi

  if ! command -v java >/dev/null 2>&1; then
    echo "java is not available in PATH." >&2
    exit 1
  fi

  local java_spec
  java_spec="$(java -XshowSettings:properties -version 2>&1 | awk '/java.specification.version = / { print $3; exit }')"
  if [[ "${java_spec}" != "17" ]]; then
    echo "Expected Java 17, but found Java ${java_spec:-unknown}." >&2
    exit 1
  fi
}

run_bootstrap_if_requested() {
  if [[ "${BOOTSTRAP_FIRST}" != "1" ]]; then
    return
  fi

  bash "${SCRIPT_DIR}/bootstrap-backend-server.sh" \
    --host "${DEPLOY_HOST}" \
    --user "${DEPLOY_USER}" \
    --port "${DEPLOY_PORT}"
}

build_release_layout() {
  echo "==> Building backend release layout"
  bash "${ROOT_DIR}/gradlew" :backend:life-boot:prepareReleaseLayout
}

find_release_app_jar() {
  local jar_path="${ROOT_DIR}/backend/life-boot/build/release/life-app.jar"
  if [[ ! -f "${jar_path}" ]]; then
    echo "Could not find release app jar at ${jar_path}." >&2
    exit 1
  fi
  printf '%s\n' "${jar_path}"
}

find_release_lib_dir() {
  local lib_dir="${ROOT_DIR}/backend/life-boot/build/release/lib"
  if [[ ! -d "${lib_dir}" ]]; then
    echo "Could not find release lib directory at ${lib_dir}." >&2
    exit 1
  fi
  printf '%s\n' "${lib_dir}"
}

create_lib_archive() {
  local lib_dir="$1"
  local archive_path
  archive_path="$(mktemp "${TMPDIR:-/tmp}/life-app-lib.XXXXXX.tar.gz")"
  tar -czf "${archive_path}" -C "${lib_dir}" .
  printf '%s\n' "${archive_path}"
}

ensure_remote_ready() {
  local ssh_target="${DEPLOY_USER}@${DEPLOY_HOST}"
  ssh -p "${DEPLOY_PORT}" "${ssh_target}" \
    "test -x /opt/life/bin/deploy-life.sh && test -f /etc/life/application-prod.yml && grep -q -- '--app-jar' /opt/life/bin/deploy-life.sh && grep -q -- 'current/lib/\\*' /etc/systemd/system/life-app.service && grep -q -- 'SPRING_PROFILES_ACTIVE=prod' /etc/systemd/system/life-app.service && grep -q -- 'SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:/etc/life/' /etc/systemd/system/life-app.service"
}

upload_and_deploy() {
  local jar_path="$1"
  local lib_archive_path="${2:-}"
  local ssh_target="${DEPLOY_USER}@${DEPLOY_HOST}"
  local release_ts git_sha remote_app_upload_path app_upload_name remote_lib_upload_path lib_upload_name remote_command

  release_ts="$(date '+%Y%m%d%H%M%S')"
  git_sha="$(git -C "${ROOT_DIR}" rev-parse --short HEAD 2>/dev/null || echo manual)"
  app_upload_name="life-app-${release_ts}-${git_sha}.jar"
  remote_app_upload_path="/opt/life/uploads/${app_upload_name}"

  echo "==> Creating remote upload directory"
  ssh -p "${DEPLOY_PORT}" "${ssh_target}" "mkdir -p /opt/life/uploads"

  echo "==> Uploading ${app_upload_name}"
  scp -P "${DEPLOY_PORT}" "${jar_path}" "${ssh_target}:${remote_app_upload_path}"

  remote_command="KEEP_RELEASES='${KEEP_RELEASES}' HEALTH_URL='${HEALTH_URL}' UPDATE_LIB='${UPDATE_LIB}' /opt/life/bin/deploy-life.sh --app-jar '${remote_app_upload_path}' --update-lib '${UPDATE_LIB}'"

  if [[ "${UPDATE_LIB}" == "yes" ]]; then
    lib_upload_name="life-app-lib-${release_ts}-${git_sha}.tar.gz"
    remote_lib_upload_path="/opt/life/uploads/${lib_upload_name}"
    echo "==> Uploading ${lib_upload_name}"
    scp -P "${DEPLOY_PORT}" "${lib_archive_path}" "${ssh_target}:${remote_lib_upload_path}"
    remote_command="${remote_command} --lib-archive '${remote_lib_upload_path}'"
  fi

  echo "==> Triggering remote deploy"
  ssh -p "${DEPLOY_PORT}" "${ssh_target}" "${remote_command}"
}

ensure_local_java17
run_bootstrap_if_requested
build_release_layout

JAR_PATH="$(find_release_app_jar)"
LIB_ARCHIVE_PATH=""

if [[ "${UPDATE_LIB}" == "yes" ]]; then
  LIB_DIR="$(find_release_lib_dir)"
  LIB_ARCHIVE_PATH="$(create_lib_archive "${LIB_DIR}")"
fi

echo "==> Verifying remote deploy script"
if ! ensure_remote_ready; then
  echo "Remote backend deploy assets are missing or outdated. Run scripts/bootstrap-backend-server.sh first or add --bootstrap." >&2
  exit 1
fi

upload_and_deploy "${JAR_PATH}" "${LIB_ARCHIVE_PATH}"

if [[ -n "${LIB_ARCHIVE_PATH}" && -f "${LIB_ARCHIVE_PATH}" ]]; then
  rm -f "${LIB_ARCHIVE_PATH}"
fi

echo "==> Release finished"
