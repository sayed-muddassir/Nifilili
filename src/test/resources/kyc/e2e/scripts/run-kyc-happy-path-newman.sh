#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../../.." && pwd)"
COLLECTION_FILE="$ROOT_DIR/src/test/resources/kyc/e2e/postman/KYC-Happy-Path.postman_collection.json"
ENV_FILE="$ROOT_DIR/src/test/resources/kyc/e2e/postman/KYC-Happy-Path.postman_environment.json"

if ! command -v newman >/dev/null 2>&1; then
  echo "newman is required. Install using: npm install -g newman"
  exit 1
fi

newman run "$COLLECTION_FILE" -e "$ENV_FILE"
