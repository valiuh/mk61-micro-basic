#!/usr/bin/env bash
set -euo pipefail

required=(
  ORG_GRADLE_PROJECT_mavenCentralUsername
  ORG_GRADLE_PROJECT_mavenCentralPassword
  ORG_GRADLE_PROJECT_signingInMemoryKey
  ORG_GRADLE_PROJECT_signingInMemoryKeyId
  ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
)

missing=()
for key in "${required[@]}"; do
  if [ -z "${!key:-}" ]; then
    missing+=("${key}")
  fi
done

if [ ${#missing[@]} -ne 0 ]; then
  echo "Missing required publish secrets: ${missing[*]}"
  exit 1
fi

echo "All required Maven publish secrets are present."
