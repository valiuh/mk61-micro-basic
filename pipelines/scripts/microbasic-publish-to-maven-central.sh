#!/usr/bin/env bash
set -euo pipefail

./gradlew assertPublishSecrets publishAndReleaseToMavenCentral --stacktrace
