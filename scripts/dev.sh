#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"
command="${1:-run}"
if [[ $# -gt 0 ]]; then shift; fi
case "$command" in
    help|--help|-h)
        echo 'Usage: scripts/dev.sh [test|build|run|native-build|native-test|native|install] [CLI arguments...]'
        exit 0 ;;
esac
require_java
case "$command" in
    test) exec mvn test "$@" ;;
    build) exec mvn clean package "$@" ;;
    run)
        artifact="$(project_value project.artifactId)"
        mvn -q -DskipTests package
        exec java -jar "target/$artifact.jar" "$@" ;;
    native-build)
        require_native_image
        exec mvn -Pnative -DskipTests package "$@" ;;
    native-test)
        require_native_image
        exec mvn -Pnative test "$@" ;;
    native)
        artifact="$(project_value project.artifactId)"
        [[ -x "target/$artifact" ]] || bash scripts/dev.sh native-build
        exec "target/$artifact" "$@" ;;
    install)
        [[ $# -eq 0 ]] || { echo 'ERROR: install takes no arguments.' >&2; exit 2; }
        bash scripts/dev.sh native-build
        artifact="$(project_value project.artifactId)"
        install_dir="${CLI_INSTALL_DIR:-${HOME:?}/.local/bin}"
        mkdir -p "$install_dir"
        install -m 755 "target/$artifact" "$install_dir/$artifact"
        echo "Installed: $install_dir/$artifact" ;;
    *) echo "ERROR: unknown command: $command" >&2; exit 2 ;;
esac
