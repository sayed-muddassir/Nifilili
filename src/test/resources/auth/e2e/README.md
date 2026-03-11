# Auth Happy Path E2E Tests

End-to-end tests for the auth module using Postman + Newman.

## What Is Tested

| Step | Request | Expectation |
|---|---|---|
| 01 | `POST /api/auth/register` (new user) | 201 + access token + refresh token + user profile |
| 02 | `POST /api/auth/register` (duplicate email) | 409 Conflict |
| 03 | `POST /api/auth/register` (weak password) | 400 Bad Request |
| 04 | `POST /api/auth/login` (registered user) | 200 + dual tokens + roles |
| 05 | `POST /api/auth/refresh` (token rotation) | 200 + new access token + rotated refresh token |
| 06 | `GET  /api/auth/me` (valid Bearer token) | 200 + profile + emailVerified |
| 07 | `GET  /api/auth/me` (no token) | 401 Unauthorized |
| 08 | `POST /api/auth/logout` (single device) | 204 No Content |
| 09 | `POST /api/auth/login` (admin — regression) | 200 + ROLE_ADMIN + dual tokens |
| 10 | `POST /api/auth/logout-all` (all devices) | 204 No Content |

## Prerequisites

- Application running on `http://localhost:8080`
- Newman installed globally: `npm install -g newman`

## One-Click Run

```bash
bash src/test/resources/auth/e2e/scripts/run-auth-happy-path-newman.sh
```

Each run uses a timestamp-based `run_id` so a fresh user is registered every time — no manual DB cleanup required.

## JSON Report

After each run a JSON report is saved to:
```
src/test/resources/auth/e2e/reports/auth-happy-path-<run_id>.json
```

## Customisation

| Variable | Default | Override |
|---|---|---|
| `base_url` | `http://localhost:8080` | Edit environment file |
| `admin_username` | `admin` | Edit environment file |
| `admin_password` | `admin` | Edit environment file |
| `run_id` | `$(date +%s)` | `--env-var run_id=my_value` |
