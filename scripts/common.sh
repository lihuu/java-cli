#!/usr/bin/env bash
# Shared build environment; source from another script.
set -euo pipefail
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

require_java() {
    if [[ -z "${JAVA_HOME:-}" && -x /usr/libexec/java_home ]]; then
        export JAVA_HOME="$(/usr/libexec/java_home -v 25)"
    fi
    if [[ -n "${JAVA_HOME:-}" ]]; then
        export PATH="$JAVA_HOME/bin:$PATH"
    fi
    local release
    release="$(java -XshowSettings:properties -version 2>&1 | sed -n 's/^[[:space:]]*java.specification.version = //p')"
    if [[ "$release" != "25" ]]; then
        echo 'ERROR: select JDK 25 with JAVA_HOME (GraalVM for native builds).' >&2
        exit 1
    fi
}

require_native_image() {
    require_java
    command -v native-image >/dev/null || {
        echo 'ERROR: native-image is missing; select GraalVM for JDK 25 with JAVA_HOME.' >&2
        exit 1
    }
}

project_value() {
    mvn -q -N -Dstyle.color=never help:evaluate -Dexpression="$1" -DforceStdout
}
