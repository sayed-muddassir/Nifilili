#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../../.." && pwd)"
COLLECTION_FILE="$ROOT_DIR/src/test/resources/business/e2e/postman/Business-Location-Admin.postman_collection.json"
ENV_FILE="$ROOT_DIR/src/test/resources/business/e2e/postman/Business-Location-Admin.postman_environment.json"

if ! command -v newman >/dev/null 2>&1; then
  echo "newman is required. Install using: npm install -g newman"
  exit 1
fi

newman run "$COLLECTION_FILE" -e "$ENV_FILE"
