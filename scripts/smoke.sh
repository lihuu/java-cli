#!/usr/bin/env bash
set -euo pipefail
if [[ $# -ne 1 || ! -x "$1" ]]; then
    echo 'Usage: scripts/smoke.sh <native-executable>' >&2
    exit 2
fi
binary="$1"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
"$binary" --help > "$tmp/help"
"$binary" --version > "$tmp/version"
"$binary" hello --name Ada > "$tmp/hello"
printf 'Hello, Ada!\n' > "$tmp/expected"
cmp "$tmp/hello" "$tmp/expected"
if "$binary" --unknown > "$tmp/out" 2> "$tmp/err"; then
    echo 'ERROR: unknown option unexpectedly succeeded.' >&2
    exit 1
else
    code=$?
    [[ "$code" -eq 2 ]]
fi
[[ ! -s "$tmp/out" && -s "$tmp/err" ]]
grep -q 'Usage:' "$tmp/help"
[[ -s "$tmp/version" ]]
if LC_ALL=C grep -q $'\033' "$tmp/help" "$tmp/version" "$tmp/hello" "$tmp/err"; then
    echo 'ERROR: redirected output contains ANSI escapes.' >&2
    exit 1
fi
echo "Native smoke passed: $binary"
