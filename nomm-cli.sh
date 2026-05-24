#!/bin/bash
# NOMM Headless CLI wrapper
# Uses the local AppImage build

NOMM_APPIMAGE="/home/tjt/src/llm/mayor/projects/nomm-headless-cli/NOMM/composeApp/build/compose/binaries/main/AppImage/extracted/squashfs-root/bin/NOMM"

if [ ! -f "$NOMM_APPIMAGE" ]; then
    echo "Error: NOMM binary not found at $NOMM_APPIMAGE" >&2
    echo "Run 'gradlew :composeApp:packageDistributionForCurrentOS' first" >&2
    exit 1
fi

source "/home/tjt/.sdkman/bin/sdkman-init.sh"
export JAVA_HOME="/home/tjt/.sdkman/candidates/java/current"

exec "$NOMM_APPIMAGE" "$@"
