#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out
javac --release 25 -d out src/model/*.java src/service/*.java src/ui/*.java src/App.java
java -cp out App
