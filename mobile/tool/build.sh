#!/usr/bin/env bash
# Build dates use the project's timezone, consistently on developer machines and CI.
set -euo pipefail
cd "$(dirname "$0")/.."
mobile_build_date=$(TZ=Asia/Shanghai date +%Y-%m-%d)
exec flutter build "$@" "--dart-define=BUILD_DATE=$mobile_build_date"
