# KYC Module E2E (Happy Path)

This folder provides a self-explanatory, handover-ready happy-path automation pack for the KYC module.

## What is included

- `src/test/resources/kyc/e2e/postman/KYC-Happy-Path.postman_collection.json`
  - Step-by-step Postman collection with variable chaining.
- `src/test/resources/kyc/e2e/postman/KYC-Happy-Path.postman_environment.json`
  - Postman environment template.
- `src/test/resources/kyc/e2e/scripts/run-kyc-happy-path-newman.sh`
  - Newman runner for the Postman collection.

## Flow covered (happy path)

1. Admin login
2. User login
3. Admin lists pending KYC applications (captures `business_id`)
4. Admin views KYC detail
5. Admin rejects KYC with feedback
6. Owner checks KYC status (sees correction banner with rejection reason)
7. Owner lists uploaded documents (sees per-document review status)
8. Owner re-uploads corrected document
9. Owner resubmits for review
10. Admin lists pending KYC applications (resubmission visible)
11. Admin approves KYC
12. Owner verifies KYC status is APPROVED
13. Admin lists approved KYC applications
14. Public checks verification badge (verified = true)

## Prerequisites

- Running application instance with reachable base URL.
- Seeded admin and user credentials.
- At least one business that has submitted for KYC review (run the Business Happy Path first).
- For scripts: `newman` required (`npm install -g newman`).

## Run options

### Option A: Newman

1. Update values in `KYC-Happy-Path.postman_environment.json`.
2. Run:

```bash
src/test/resources/kyc/e2e/scripts/run-kyc-happy-path-newman.sh
```

### Option B: Postman GUI

1. Import both the collection and environment JSON files.
2. Select the "KYC Happy Path Environment".
3. Set `business_id` and `document_definition_id` if not auto-captured from step 03.
4. Run the collection sequentially.

## Handover notes

- Run the Business Happy Path collection first to ensure a business exists in PENDING KYC state.
- Keep this flow as the baseline KYC verification contract.
- Add one file per new scenario (`unauthorized path`, `validation errors`, `claim flow`) to keep maintenance simple.
- For CI, run Newman against a dedicated test environment.
