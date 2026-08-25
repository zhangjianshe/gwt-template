#!/bin/bash
# Exit immediately if a command exits with a non-zero status
set -e
docker build  -t hub.cangling.cn/cangling/gwt-template:latest .
