#!/usr/bin/env bash
# Runs the portal math check (needs only a JDK or JRE 17+, no Minecraft, no network).
set -e
cd "$(dirname "$0")/portalmath-check"
OUT="$(mktemp -d)"
if command -v javac >/dev/null 2>&1; then JAVAC="javac"; else JAVAC="java -m jdk.compiler/com.sun.tools.javac.Main"; fi
$JAVAC -d "$OUT" ../../src/main/java/me/ichun/mods/portalgunclassic/common/portal/PortalMath.java shim/*.java Check.java
java -cp "$OUT" Check
