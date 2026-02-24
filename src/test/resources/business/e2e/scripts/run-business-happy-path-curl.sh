#!/usr/bin/env bash
set -euo pipefail

# Required environment variables:
# BUSINESS_E2E_BASE_URL, BUSINESS_E2E_ADMIN_USERNAME, BUSINESS_E2E_ADMIN_PASSWORD,
# BUSINESS_E2E_USER_USERNAME, BUSINESS_E2E_USER_PASSWORD

for required in \
  BUSINESS_E2E_BASE_URL \
  BUSINESS_E2E_ADMIN_USERNAME \
  BUSINESS_E2E_ADMIN_PASSWORD \
  BUSINESS_E2E_USER_USERNAME \
  BUSINESS_E2E_USER_PASSWORD; do
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

echo "[1/15] Admin login"
ADMIN_TOKEN="$(curl -sS -X POST "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"usernameOrEmail\":\"$BUSINESS_E2E_ADMIN_USERNAME\",\"password\":\"$BUSINESS_E2E_ADMIN_PASSWORD\"}" | jq -r '.accessToken')"

AUTH_ADMIN=( -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' )

echo "[2/15] Create vertical"
VERTICAL_ID="$(curl -sS -X POST "$BASE_URL/api/v1/admin/config/verticals" "${AUTH_ADMIN[@]}" \
  -d "{\"name\":\"Food Services $RUN_ID\",\"slug\":\"food-services-$RUN_ID\",\"description\":\"Vertical for E2E\",\"iconUrl\":\"https://example.com/icons/food.png\",\"active\":true}" | jq -r '.id')"

echo "[3/15] Create category"
CATEGORY_ID="$(curl -sS -X POST "$BASE_URL/api/v1/admin/config/categories" "${AUTH_ADMIN[@]}" \
  -d "{\"businessVerticalId\":$VERTICAL_ID,\"parentCategoryId\":null,\"name\":\"Cafe $RUN_ID\",\"slug\":\"cafe-$RUN_ID\",\"description\":\"Category for E2E\",\"iconUrl\":\"https://example.com/icons/cafe.png\",\"activeStatus\":true}" | jq -r '.id')"

echo "[4/15] Create section"
SECTION_ID="$(curl -sS -X POST "$BASE_URL/api/v1/admin/config/sections" "${AUTH_ADMIN[@]}" \
  -d "{\"verticalId\":$VERTICAL_ID,\"categoryId\":$CATEGORY_ID,\"name\":\"menu_details_$RUN_ID\",\"label\":\"Menu Details\",\"promptText\":\"Please provide menu details\",\"required\":true,\"allowMultiple\":false,\"groupable\":false}" | jq -r '.id')"

echo "[5/15] Create section field"
curl -sS -X POST "$BASE_URL/api/v1/admin/config/sections/$SECTION_ID/fields" "${AUTH_ADMIN[@]}" \
  -d '{"name":"primary_dish","label":"Primary Dish","type":"TEXT","options":[],"required":true,"allowMultiple":false}' >/dev/null

echo "[6/15] Create attribute definition"
ATTRIBUTE_ID="$(curl -sS -X POST "$BASE_URL/api/v1/admin/config/attributes" "${AUTH_ADMIN[@]}" \
  -d "{\"verticalId\":$VERTICAL_ID,\"name\":\"service_mode_$RUN_ID\",\"label\":\"Service Mode\",\"type\":\"DROPDOWN\",\"options\":[\"DINE_IN\",\"TAKEAWAY\"],\"required\":true,\"allowMultiple\":false,\"prompt\":\"Choose one mode\"}" | jq -r '.id')"

echo "[7/15] Create document definition"
DOCUMENT_DEFINITION_ID="$(curl -sS -X POST "$BASE_URL/api/v1/admin/config/documents" "${AUTH_ADMIN[@]}" \
  -d "{\"verticalId\":$VERTICAL_ID,\"name\":\"business_registration_$RUN_ID\",\"label\":\"Business Registration\",\"allowedExtensions\":[\"pdf\",\"jpg\"],\"maxFileSize\":5242880,\"required\":true}" | jq -r '.id')"

echo "[8/15] User login"
USER_TOKEN="$(curl -sS -X POST "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"usernameOrEmail\":\"$BUSINESS_E2E_USER_USERNAME\",\"password\":\"$BUSINESS_E2E_USER_PASSWORD\"}" | jq -r '.accessToken')"
AUTH_USER=( -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' )

echo "[9/15] Create business"
BUSINESS_ID="$(curl -sS -X POST "$BASE_URL/api/v1/business" "${AUTH_USER[@]}" \
  -d "{\"verticalId\":$VERTICAL_ID,\"name\":\"Happy Path Cafe $RUN_ID\",\"legalName\":\"Happy Path Cafe Pvt Ltd\",\"municipalityId\":1,\"wardNumber\":1,\"toleName\":\"Downtown\",\"addressField1\":\"Main Road\",\"addressField2\":\"Near Landmark\",\"postalCode\":\"44600\",\"website\":\"https://happy.example.com\"}" | jq -r '.businessId')"

echo "[10/15] Update profile"
curl -sS -X PUT "$BASE_URL/api/v1/business/$BUSINESS_ID/profile" "${AUTH_USER[@]}" \
  -d '{"businessSummary":"E2E onboarding run","legalName":"Happy Path Cafe Pvt Ltd","addressField2":"Updated Landmark","contacts":{"phone":"+977-9800000000","email":"owner@example.com"},"businessHours":{"mon":"09:00-18:00"},"latitude":27.7172,"longitude":85.3240}' >/dev/null

echo "[11/15] Update categories"
curl -sS -X PUT "$BASE_URL/api/v1/business/$BUSINESS_ID/categories" "${AUTH_USER[@]}" \
  -d "{\"categoryIds\":[$CATEGORY_ID]}" >/dev/null

echo "[12/15] Save section data"
curl -sS -X POST "$BASE_URL/api/v1/business/$BUSINESS_ID/sections/$SECTION_ID" "${AUTH_USER[@]}" \
  -d '{"sectionGroupId":null,"fieldValues":{"primary_dish":"Mocha"}}' >/dev/null

echo "[13/15] Save attribute data"
curl -sS -X PUT "$BASE_URL/api/v1/business/$BUSINESS_ID/attributes" "${AUTH_USER[@]}" \
  -d "{\"attributeId\":$ATTRIBUTE_ID,\"attributeValue\":\"DINE_IN\"}" >/dev/null

echo "[14/15] Upload document"
curl -sS -X POST "$BASE_URL/api/v1/business/$BUSINESS_ID/documents" "${AUTH_USER[@]}" \
  -d "{\"documentDefinitionId\":$DOCUMENT_DEFINITION_ID,\"fileUrl\":\"https://example.com/docs/registration.pdf\",\"fileName\":\"registration.pdf\"}" >/dev/null

echo "[15/15] Submit for review"
curl -sS -X POST "$BASE_URL/api/v1/business/$BUSINESS_ID/submit" "${AUTH_USER[@]}" \
  -d '{"message":"Please review my onboarding request"}' >/dev/null

echo "Business happy-path E2E finished successfully. Business ID: $BUSINESS_ID"
