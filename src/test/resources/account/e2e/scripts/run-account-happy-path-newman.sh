#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# Account Happy Path E2E — Newman runner
# Usage:  bash src/test/resources/account/e2e/scripts/run-account-happy-path-newman.sh
# Prereq: npm install -g newman
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COLLECTION="$SCRIPT_DIR/../postman/Account-Happy-Path.postman_collection.json"
ENVIRONMENT="$SCRIPT_DIR/../postman/Account-Happy-Path.postman_environment.json"
REPORTS_DIR="$SCRIPT_DIR/../reports"

mkdir -p "$REPORTS_DIR"

# Unique run_id ensures each test run registers a fresh user (avoids conflicts)
RUN_ID=$(date +%s)

echo "────────────────────────────────────────────────"
echo "  Account Happy Path E2E"
echo "  run_id : $RUN_ID"
echo "────────────────────────────────────────────────"

newman run "$COLLECTION" \
  --environment "$ENVIRONMENT" \
  --env-var "run_id=$RUN_ID" \
  --reporters cli,json \
  --reporter-json-export "$REPORTS_DIR/account-happy-path-$RUN_ID.json"

echo ""
echo "✓  Report: $REPORTS_DIR/account-happy-path-$RUN_ID.json"
