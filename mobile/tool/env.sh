# Source from the repository: . mobile/tool/env.sh
# Override ANDROID_HOME, FLUTTER_ROOT or JAVA_HOME before sourcing if needed.
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export FLUTTER_ROOT="${FLUTTER_ROOT:-$HOME/tools/flutter-sdk}"
if [ -z "${JAVA_HOME:-}" ] && [ -d /opt/android-studio/jbr ]; then
    export JAVA_HOME=/opt/android-studio/jbr
fi
export PATH="$FLUTTER_ROOT/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
