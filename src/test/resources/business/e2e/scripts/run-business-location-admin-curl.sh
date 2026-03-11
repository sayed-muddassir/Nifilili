#!/usr/bin/env bash
set -euo pipefail

# Required environment variables:
# BUSINESS_E2E_BASE_URL, BUSINESS_E2E_ADMIN_USERNAME, BUSINESS_E2E_ADMIN_PASSWORD

for required in \
  BUSINESS_E2E_BASE_URL \
  BUSINESS_E2E_ADMIN_USERNAME \
  BUSINESS_E2E_ADMIN_PASSWORD; do
  if [[ -z "${!required:-}" ]]; then
    echo "Missing required env var: $required"
    exit 1
  fi
done

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required. Install jq and retry."
  exit 1
fi

BASE_URL="$BUSINESS_E2E_BASE_URL"
RUN_ID="$(date +%s)"

echo "[1/6] Admin login"
ADMIN_TOKEN="$(curl -sS -X POST "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"usernameOrEmail\":\"$BUSINESS_E2E_ADMIN_USERNAME\",\"password\":\"$BUSINESS_E2E_ADMIN_PASSWORD\"}" | jq -r '.accessToken')"

AUTH_ADMIN=( -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' )

echo "[2/6] Create province"
PROVINCE_ID="$(curl -sS -X POST "$BASE_URL/api/v1/admin/business-locations/provinces" "${AUTH_ADMIN[@]}" \
  -d "{\"name\":\"Bagmati $RUN_ID\"}" | jq -r '.id')"

echo "[3/6] Create district"
DISTRICT_ID="$(curl -sS -X POST "$BASE_URL/api/v1/admin/business-locations/districts" "${AUTH_ADMIN[@]}" \
  -d "{\"provinceId\":$PROVINCE_ID,\"name\":\"Kathmandu $RUN_ID\"}" | jq -r '.id')"

echo "[4/6] Create municipality"
MUNICIPALITY_ID="$(curl -sS -X POST "$BASE_URL/api/v1/admin/business-locations/municipalities" "${AUTH_ADMIN[@]}" \
  -d "{\"districtId\":$DISTRICT_ID,\"name\":\"Kathmandu Metropolitan $RUN_ID\",\"type\":\"metropolitan\"}" | jq -r '.id')"

echo "[5/6] Verify districts under province"
curl -sS -X GET "$BASE_URL/api/v1/admin/business-locations/provinces/$PROVINCE_ID/districts" \
  -H "Authorization: Bearer $ADMIN_TOKEN" >/dev/null

echo "[6/6] Verify municipalities under district"
curl -sS -X GET "$BASE_URL/api/v1/admin/business-locations/districts/$DISTRICT_ID/municipalities" \
  -H "Authorization: Bearer $ADMIN_TOKEN" >/dev/null

echo "Business location admin E2E finished successfully. Province ID: $PROVINCE_ID, District ID: $DISTRICT_ID, Municipality ID: $MUNICIPALITY_ID"
