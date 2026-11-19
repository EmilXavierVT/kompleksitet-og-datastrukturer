#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$project_dir"
mkdir -p target/classes
javac --release 17 -encoding UTF-8 -Xlint:all -d target/classes src/main/java/complexity/*.java

if [[ "${1:-}" == "--check" ]]; then
    mkdir -p target/test-classes
    javac --release 17 -encoding UTF-8 -Xlint:all -cp target/classes \
        -d target/test-classes src/test/java/complexity/StudentChecks.java
    java -cp target/classes:target/test-classes complexity.StudentChecks
else
    java -Xms256m -Xmx1g -cp target/classes complexity.ComplexityDemo "$@"
fi
