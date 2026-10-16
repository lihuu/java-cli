#!/usr/bin/env python3
"""Summarize runtime dependencies and licenses declared by their local Maven POMs.

This is a dependency inventory, not a complete license-compliance audit. Missing
or inherited license declarations remain visible for the release owner to review.
"""
import pathlib
import re
import sys
import xml.etree.ElementTree as ET

raw, repository, output = map(pathlib.Path, sys.argv[1:])
coordinates = dict.fromkeys(re.findall(r"([\w.-]+):([\w.-]+):jar:([^:\s]+):(compile|runtime)\b", raw.read_text()))
if not coordinates:
    sys.exit("No runtime dependencies found; inspect dependency:list output")
ns = {"m": "http://maven.apache.org/POM/4.0.0"}
lines = ["Third-party runtime dependencies and declared licenses", ""]
for group, artifact, version, scope in coordinates:
    lines.append(f"{group}:{artifact}:{version}")
    pom = repository / group.replace(".", "/") / artifact / version / f"{artifact}-{version}.pom"
    try:
        tree = ET.parse(pom)
        licenses = tree.findall("m:licenses/m:license", ns)
        for license in licenses:
            name = license.findtext("m:name", default="unknown", namespaces=ns)
            url = license.findtext("m:url", default="", namespaces=ns)
            lines.append(f"  {name}: {url}")
        if not licenses:
            lines.append("  No direct license declaration; review upstream/inherited metadata.")
    except (OSError, ET.ParseError) as failure:
        lines.append(f"  Unable to read license metadata: {failure}")
    lines.append("")
output.write_text("\n".join(lines), encoding="utf-8")
