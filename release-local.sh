#!/bin/bash
# Exit immediately if a command exits with a non-zero status
set -e

# Configuration
IMAGE_NAME="hub.cangling.cn/cangling/gwt-template"
VERSION="1.0"

echo "### Step 1: Maven Build & GWT Compilation ###"
mvn clean compile gwt:compile package install -DskipTests

echo "### Step 2: Building Docker Image ###"
docker build -t ${IMAGE_NAME}:${VERSION} .

echo "### Step 3: Tagging and Pushing ###"
docker tag ${IMAGE_NAME}:${VERSION} ${IMAGE_NAME}:latest
docker push ${IMAGE_NAME}:${VERSION}
docker push ${IMAGE_NAME}:latest

echo "Done! Image ${IMAGE_NAME}:${VERSION} pushed successfully."

ssh root@dev.cangling.cn -p2222 ~/soft/dev/dev update