#!/usr/bin/env bash
# Build and package locally. Publishing is handled by the tag-triggered workflow.
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"
[[ $# -eq 0 ]] || { echo 'Usage: scripts/release.sh' >&2; exit 2; }
require_native_image
command -v python3 >/dev/null
artifact="$(project_value project.artifactId)"
version="$(project_value project.version)"
[[ "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || {
    echo "ERROR: release version must be X.Y.Z, got $version" >&2; exit 1;
}
case "$(uname -s)" in
    Darwin) platform=macos ;;
    Linux) platform=linux ;;
    *) echo 'ERROR: release.sh supports macOS and Linux.' >&2; exit 1 ;;
esac
case "$(uname -m)" in
    arm64|aarch64) architecture=aarch64 ;;
    x86_64|amd64) architecture=x86_64 ;;
    *) echo 'ERROR: unsupported architecture.' >&2; exit 1 ;;
esac
mvn -B -Pnative clean package
bash scripts/smoke.sh "target/$artifact"
name="$artifact-$version-$platform-$architecture"
mkdir -p dist
staging="$(mktemp -d "$REPO_ROOT/dist/.package.XXXXXX")"
trap 'rm -rf "$staging"' EXIT
mkdir "$staging/$name"
install -m 755 "target/$artifact" "$staging/$name/$artifact"
cp README.md "$staging/$name/README.md"
cp -R release-assets/. "$staging/$name/"
mvn -B -Dstyle.color=never dependency:list -DincludeScope=runtime -DoutputFile="$staging/dependencies.txt"
repository="$(project_value settings.localRepository)"
python3 scripts/third-party-notices.py "$staging/dependencies.txt" "$repository" "$staging/$name/THIRD-PARTY-NOTICES.txt"
# Preserve upstream license/notice resources shipped by runtime JARs.
python3 - "$artifact" "$staging/$name" <<'PYTHON'
import pathlib, re, sys, zipfile
artifact, bundle = sys.argv[1], pathlib.Path(sys.argv[2])
with zipfile.ZipFile(f"target/{artifact}.jar") as jar:
    for entry in jar.infolist():
        if entry.filename.startswith("META-INF/") and re.search(r"(?:^|/)(?:LICENSE|NOTICE)[^/]*$", entry.filename, re.I):
            target = bundle / entry.filename
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(jar.read(entry))
PYTHON
tar -czf "dist/$name.tar.gz" -C "$staging" "$name"
(cd dist && shasum -a 256 "$name.tar.gz" > "$name.tar.gz.sha256")
echo "Bundle: dist/$name.tar.gz"
