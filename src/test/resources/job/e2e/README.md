# Job Happy Path E2E Tests

End-to-end tests for the job module using Postman + Newman.

## What Is Tested

| Step | Request | Expectation |
|---|---|---|
| 01 | `POST /api/auth/login` (admin) | 200 + admin token |
| 02 | `POST /api/auth/login` (recruiter) | 200 + user token |
| 03 | `POST /api/v1/admin/jobs/categories` | 201 + category ID |
| 04 | `GET  /api/v1/admin/jobs/categories` | 200 + array of categories |
| 05 | `POST /api/v1/business/jobs` | 201 + job ID |
| 06 | `PUT  /api/v1/business/jobs/{id}` | 200 + updated job |
| 07 | `POST /api/v1/business/jobs/{id}/questions` | 201 + question ID |
| 08 | `GET  /api/v1/business/jobs/{id}/questions` | 200 + at least 1 question |
| 09 | `POST /api/v1/business/jobs/{id}/publish` | 200 + job published |
| 10 | `GET  /api/v1/jobs?keyword=Backend` | 200 + array of open jobs |
| 11 | `GET  /api/v1/jobs/{id}` | 200 + job details with title |
| 12 | `POST /api/v1/jobs/{id}/apply` | 201 + application ID |
| 13 | `GET  /api/v1/jobs/applications/{userId}` | 200 + at least 1 application |
| 14 | `GET  /api/v1/business/jobs/{id}/applications` | 200 + at least 1 application |
| 15 | `POST .../applications/{id}/status` (REVIEWED) | 200 + status changed |
| 16 | `POST .../applications/{id}/status` (SHORTLISTED) | 200 + status changed |
| 17 | `POST .../applications/{id}/status` (INTERVIEWING) | 200 + status changed |
| 18 | `POST .../applications/{id}/status` (HIRED) | 200 + status changed |
| 19 | `GET  .../applications/{id}/timeline` | 200 + 5 history entries, final = HIRED |
| 20 | `POST /api/v1/business/jobs/{id}/close` | 200 + job closed |
| 21 | `POST /api/v1/business/jobs/{id}/reopen` | 200 + job reopened |

## Flow Covered

1. **Admin setup** — login + create job category
2. **Recruiter creates job** — create, update, add screening question, publish
3. **Public discovery** — search open jobs, view job details
4. **Applicant applies** — submit application with answers, view my applications
5. **Recruiter pipeline** — view applications, advance through RECEIVED -> REVIEWED -> SHORTLISTED -> INTERVIEWING -> HIRED
6. **Timeline audit** — verify 5 status history entries ending at HIRED
7. **Lifecycle management** — close and reopen job opening

## Prerequisites

- Application running on `http://localhost:8080`
- Newman installed globally: `npm install -g newman`
- A seeded admin account (credentials in environment file)
- A seeded recruiter/user account with an existing business (`business_id` in environment)

## One-Click Run

```bash
bash src/test/resources/job/e2e/scripts/run-job-happy-path-newman.sh
```

Each run uses a timestamp-based `run_id` so fresh resources (category name, job title) are created every time — no manual DB cleanup required.

## JSON Report

After each run a JSON report is saved to:
```
src/test/resources/job/e2e/reports/job-happy-path-<run_id>.json
```

## Customisation

| Variable | Default | Override |
|---|---|---|
| `base_url` | `http://localhost:8080` | Edit environment file |
| `admin_username` | `admin@example.com` | Edit environment file |
| `admin_password` | `admin-password` | Edit environment file |
| `user_username` | `user@example.com` | Edit environment file |
| `user_password` | `user-password` | Edit environment file |
| `business_id` | `1` | Edit environment file |
| `applicant_user_id` | `2` | Edit environment file |
| `run_id` | `$(date +%s)` | `--env-var run_id=my_value` |

## File Structure

```
src/test/resources/job/e2e/
├── postman/
│   ├── Job-Happy-Path.postman_collection.json   # 21-request collection
│   └── Job-Happy-Path.postman_environment.json   # Environment variables
├── scripts/
│   └── run-job-happy-path-newman.sh              # Newman runner script
├── reports/                                       # Auto-generated JSON reports
└── README.md                                      # This file
```
