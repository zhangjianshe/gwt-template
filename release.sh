#!/bin/bash
# 每一个新的版本 都会产生一个新的分支 在新的分支上打上标签 然后推送到中央仓库
# master
#    |---> v1.0.12
#    |        |-> TAG v1.0.12
#  version.txt is current released version
set -e
VERSION_FILE=version.txt
bump_version() {
    local current_version=$1
    # Strip any trailing -SNAPSHOT or other qualifiers
    local base_version=$(echo "$current_version" | sed 's/-.*//')

    # Split version into major, minor, patch
    if [[ "$base_version" =~ ([0-9]+)\.([0-9]+)\.([0-9]+) ]]; then
        local major=${BASH_REMATCH[1]}
        local minor=${BASH_REMATCH[2]}
        local patch=${BASH_REMATCH[3]}

        # Increment the patch number
        local new_patch=$((patch + 1))
        echo "${major}.${minor}.${new_patch}"
    else
        echo "Error: Version format ($current_version) is not recognized (expected X.Y.Z)." >&2
        return 1
    fi
}

# Function to execute a command or echo it if in dry-run mode
execute() {

    echo "Executing: $*"
    "$@"
    if [ $? -ne 0 ]; then
        echo "Error executing command: '$*'" >&2
        exit 1
    fi

}

# --- Main Script Logic ---

# Check for dry-run argument
if [[ "$1" == "--dry-run" ]]; then
    DRY_RUN=true
    echo "--- Running in DRY-RUN mode. No changes will be made to files or Git. ---"
fi

# 1. Check for prerequisite tools
if ! command -v git &> /dev/null || ! command -v sed &> /dev/null; then
    echo "Error: 'git' and 'sed' are required but not found." >&2
    exit 1
fi


# 3. Check for uncommitted changes (prevents tagging a dirty tree)
if [ -n "$(git status --porcelain)" ]; then
    echo "Error: You have uncommitted changes. Please commit or stash them before running." >&2
    exit 1
fi

# 4. Extract current version from pom.xml
echo "Reading current version from $VERSION_FILE..."
# Use grep and sed to safely extract the version from the main <version> tag
CURRENT_VERSION=$(cat "$VERSION_FILE")
if [ -z "$CURRENT_VERSION" ]; then
    echo "Error: Could not extract version from $VERSION_FILE. Ensure version.txt tag is present." >&2
    exit 1
fi

echo "Current Version: $CURRENT_VERSION"

# 5. Calculate new version
NEW_VERSION=$(bump_version "$CURRENT_VERSION")

if [ $? -ne 0 ]; then
    exit 1 # Exit if bump_version failed
fi

echo "New Version: $NEW_VERSION"

#  commit new version info to version.txt
echo "$NEW_VERSION" > version.txt
git add version.txt
git commit -m "change to new version"



TAG="v${NEW_VERSION}"
NEW_BRANCH=${TAG}
echo "New Tag: $TAG"

git push origin
#git checkout -b $NEW_BRANCH


# 8. Tag the commit
echo "Creating tag ${TAG}..."
execute git tag -a "$TAG" -m "Version ${NEW_VERSION}"

# 9. Push the tag to origin
echo "Pushing tag ${TAG} to origin..."
#execute git push origin $NEW_BRANCH
execute git push origin "$TAG"
#execute git checkout master

echo ""

echo "✅ Success! Version updated to ${NEW_VERSION}, committed, and tag ${TAG} pushed."
echo "You may now continue with your deployment or push the main branch changes."

