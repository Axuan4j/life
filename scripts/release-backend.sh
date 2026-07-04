#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

export APP_DISPLAY_NAME="user backend"
export RELEASE_GRADLE_TASK=":backend:life-boot:prepareReleaseLayout"
export LOCAL_RELEASE_DIR="backend/life-boot/build/release"
export APP_JAR_NAME="life-app.jar"
export APP_UPLOAD_PREFIX="life-app"
export LIB_ARCHIVE_PREFIX="life-app-lib"
export REMOTE_APP_ROOT="/opt/life"
export REMOTE_SERVICE_NAME="life-app"
export REMOTE_SYSTEMD_UNIT="/etc/systemd/system/life-app.service"
export REMOTE_ENV_FILE="/etc/life/life-app.env"
export REMOTE_CONFIG_FILE="/etc/life/application-prod.yml"
export DEFAULT_HEALTH_URL="http://127.0.0.1:18080/actuator/health"
export RELEASE_SCRIPT_NAME="scripts/release-backend.sh"

bash "${SCRIPT_DIR}/release-backend-common.sh" "$@"
