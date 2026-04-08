# Business Module E2E (Happy Path)

This folder provides a self-explanatory, handover-ready happy-path automation pack for the business module.

## What is included

- `src/test/java/com/nifilili/business/e2e/BusinessHappyPathE2eTest.java`
  - JUnit end-to-end happy-path API flow against a running environment.
- `src/test/resources/business/e2e/postman/Business-Happy-Path.postman_collection.json`
  - Step-by-step Postman collection with variable chaining.
- `src/test/resources/business/e2e/postman/Business-Happy-Path.postman_environment.json`
  - Postman environment template.
- `src/test/resources/business/e2e/scripts/run-business-happy-path-newman.sh`
  - Newman runner for the Postman collection.
- `src/test/resources/business/e2e/scripts/run-business-happy-path-curl.sh`
  - Pure curl + jq runner for CLI usage.

## Flow covered (happy path)

1. Admin login
2. Create vertical
3. Create category
4. Create section
5. Create section field
6. Create attribute definition
7. Create document definition
8. User login
9. Create business
10. Update profile
11. Update categories
12. Save section data
13. Save attribute data
14. Upload document
15. Submit for review
16. Upload business file (local storage API)
17. Download business file (by relative path)

## Prerequisites

- Running application instance with reachable base URL.
- Seeded admin and user credentials.
- For scripts:
  - `jq` required for curl runner.
  - `newman` required for Postman runner.

## Run options

### Option A: JUnit E2E test

Export environment variables:

```bash
export BUSINESS_E2E_BASE_URL="http://localhost:8080"
export BUSINESS_E2E_ADMIN_USERNAME="admin@example.com"
export BUSINESS_E2E_ADMIN_PASSWORD="admin-password"
export BUSINESS_E2E_USER_USERNAME="user@example.com"
export BUSINESS_E2E_USER_PASSWORD="user-password"
```

Run only this E2E class:

```bash
/bin/sh /Applications/IntelliJ\ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn \
  -Didea.version=2025.3.2 \
  -Dmaven.ext.class.path=/Applications/IntelliJ\ IDEA.app/Contents/plugins/maven/lib/maven-event-listener.jar \
  -Djansi.passthrough=true \
  -Dstyle.color=always \
  -s /Users/sayedmuddassirhussain/.m2/personal/settings.xml \
  -Dmaven.repo.local=/Users/sayedmuddassirhussain/.m2/personal/repository \
  -Dtest=BusinessHappyPathE2eTest test -f pom.xml
```

### Option B: Newman

1. Update values in `Business-Happy-Path.postman_environment.json`.
2. Run:

```bash
src/test/resources/business/e2e/scripts/run-business-happy-path-newman.sh
```

### Option C: curl script

Export same env vars as the JUnit option, then run:

```bash
src/test/resources/business/e2e/scripts/run-business-happy-path-curl.sh
```

## Handover notes

- Keep this flow as the baseline onboarding contract.
- Add one file per new scenario (`rejection path`, `unauthorized path`, `validation errors`) to keep maintenance simple.
- For CI, run Newman or the JUnit E2E test against a dedicated test environment.
