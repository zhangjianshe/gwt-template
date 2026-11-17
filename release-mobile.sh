#!/usr/bin/env bash
# Mobile releases are independent of the server's release.sh / v* tags.
set -euo pipefail
cd "$(dirname "$0")"
exec python3 mobile/tool/release.py "$@"
