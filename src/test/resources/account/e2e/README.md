# Account Happy Path E2E Tests

End-to-end tests for the account module using Postman + Newman.

## What Is Tested

| Step | Request | Expectation |
|---|---|---|
| 00 | `POST /api/auth/register` (setup) | 201 + tokens saved for subsequent requests |
| 01 | `GET  /api/v1/account/profile` (initial) | 200 + username + email + null extended fields |
| 02 | `PUT  /api/v1/account/profile` (update) | 200 + bio, city, country, gender updated |
| 03 | `GET  /api/v1/account/profile` (after update) | 200 + persisted fields |
| 04 | `POST /api/v1/account/security/change-password` | 200 + success message |
| 05 | `POST /api/auth/login` (new password) | 200 + new tokens |
| 06 | `POST /api/v1/account/security/request-password-reset` (unknown email) | 200 + generic message (anti-enumeration) |
| 07 | `POST /api/v1/account/security/resend-verification` | 200 + confirmation message |
| 08 | `GET  /api/v1/account/sessions` | 200 + session list with device info |
| 09 | `GET  /api/v1/account/sessions/history` | 200 + paginated login history |
| 10 | `GET  /api/v1/account/profile` (no token) | 401 Unauthorized |

## Prerequisites

- Application running on `http://localhost:8080`
- Newman installed globally: `npm install -g newman`

## One-Click Run

```bash
bash src/test/resources/account/e2e/scripts/run-account-happy-path-newman.sh
```

Each run uses a timestamp-based `run_id` so a fresh user is registered every time — no manual DB cleanup required.

## JSON Report

After each run a JSON report is saved to:
```
src/test/resources/account/e2e/reports/account-happy-path-<run_id>.json
```

## Customisation

| Variable | Default | Override |
|---|---|---|
| `base_url` | `http://localhost:8080` | Edit environment file |
| `run_id` | `$(date +%s)` | `--env-var run_id=my_value` |
