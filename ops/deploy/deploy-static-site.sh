#!/usr/bin/env bash

set -euo pipefail

APP_ROOT="${APP_ROOT:-/opt/life}"
KEEP_RELEASES="${KEEP_RELEASES:-5}"

usage() {
  cat <<'EOF'
Usage:
  /opt/life/bin/deploy-static-site.sh <site-name> <uploaded-archive-path>
EOF
}

log() {
  printf '[deploy-static-site] %s\n' "$*"
}

require_root() {
  if [[ "$(id -u)" -ne 0 ]]; then
    echo "deploy-static-site.sh must run as root." >&2
    exit 1
  fi
}

require_commands() {
  local cmd
  for cmd in find install ln readlink rm sort tar; do
    if ! command -v "${cmd}" >/dev/null 2>&1; then
      echo "Missing required command: ${cmd}" >&2
      exit 1
    fi
  done
}

cleanup_old_releases() {
  local releases_dir="$1"
  local count=0 dir
  while IFS= read -r dir; do
    count=$((count + 1))
    if ((count > KEEP_RELEASES)); then
      rm -rf "${dir}"
      log "Removed old release ${dir}"
    fi
  done < <(find "${releases_dir}" -mindepth 1 -maxdepth 1 -type d | sort -r)
}

if [[ $# -ne 2 ]]; then
  usage
  exit 1
fi

SITE_NAME="$1"
UPLOAD_ARCHIVE="$2"

if [[ ! -f "${UPLOAD_ARCHIVE}" ]]; then
  echo "Uploaded archive not found: ${UPLOAD_ARCHIVE}" >&2
  exit 1
fi

require_root
require_commands

site_root="${APP_ROOT}/${SITE_NAME}"
releases_dir="${site_root}/releases"
current_link="${site_root}/current"
release_id="$(date '+%Y%m%d%H%M%S')"
release_dir="${releases_dir}/${release_id}"

log "Creating release ${release_id} for ${SITE_NAME}"
install -d -o root -g root -m 755 "${releases_dir}"
install -d -o root -g root -m 755 "${release_dir}"

tar -xzf "${UPLOAD_ARCHIVE}" -C "${release_dir}"

if [[ ! -f "${release_dir}/index.html" ]]; then
  echo "index.html not found after extracting ${UPLOAD_ARCHIVE}" >&2
  rm -rf "${release_dir}"
  exit 1
fi

ln -sfn "${release_dir}" "${current_link}"
rm -f "${UPLOAD_ARCHIVE}"
cleanup_old_releases "${releases_dir}"

log "Deployment succeeded for ${SITE_NAME}"
