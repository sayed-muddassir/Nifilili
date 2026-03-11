# Business Location Admin E2E

This additive admin flow covers the hierarchical setup of business location masters without changing the existing business happy-path pack.

## Flow covered

1. Admin login
2. Create province
3. Create district under province
4. Create municipality under district
5. Verify district listing by province
6. Verify municipality listing by district

## Files

- `src/test/resources/business/e2e/postman/Business-Location-Admin.postman_collection.json`
- `src/test/resources/business/e2e/postman/Business-Location-Admin.postman_environment.json`
- `src/test/resources/business/e2e/scripts/run-business-location-admin-newman.sh`
- `src/test/resources/business/e2e/scripts/run-business-location-admin-curl.sh`

## Required environment variables for curl script

- `BUSINESS_E2E_BASE_URL`
- `BUSINESS_E2E_ADMIN_USERNAME`
- `BUSINESS_E2E_ADMIN_PASSWORD`
