# Offering Happy Path E2E Tests

End-to-end tests for the offering module using Postman + Newman.

## What Is Tested

| Step | Request | Expectation |
|---|---|---|
| 01 | `POST /api/auth/login` (admin) | 200 + admin token |
| 02 | `POST /api/auth/login` (owner) | 200 + user token |
| 03 | `POST /api/v1/admin/offering-categories` | 200 + root category ID |
| 04 | `POST /api/v1/admin/offering-categories` | 200 + child category with parent |
| 05 | `GET  /api/v1/admin/offering-categories/tree` | 200 + array of root categories |
| 06 | `PUT  /api/v1/admin/offering-categories/{id}` | 200 + name updated |
| 07 | `POST /api/v1/admin/offering-attributes` | 200 + attribute ID with options |
| 08 | `GET  /api/v1/admin/offering-attributes/category/{id}` | 200 + array of attributes |
| 09 | `PUT  /api/v1/admin/offering-attributes/{id}` | 200 + options updated |
| 10 | `POST /api/v1/offerings` | 200 + offering ID, status=DRAFT |
| 11 | `GET  /api/v1/offerings/{id}` | 200 + offering details |
| 12 | `PUT  /api/v1/offerings/{id}` | 200 + updated fields |
| 13 | `GET  /api/v1/offerings?status=DRAFT` | 200 + paginated content |
| 14 | `POST /api/v1/offerings/{id}/variants` | 200 + variant ID, status=ACTIVE |
| 15 | `PUT  /api/v1/offerings/{id}/variants/{vid}` | 200 + updated SKU/price |
| 16 | `GET  /api/v1/offerings/{id}/variants` | 200 + paginated variants |
| 17 | `POST /api/v1/variants/{vid}/attributes` | 200 + saved=true |
| 18 | `GET  /api/v1/variants/{vid}/attributes` | 200 + 2 attributes (predefined + custom) |
| 19 | `PATCH /api/v1/variants/{vid}/attributes/{aid}` | 200 + status=UPDATED |
| 20 | `POST /api/v1/discounts` | 200 + discount ID, status=ACTIVE |
| 21 | `GET  /api/v1/discounts/offering/{id}` | 200 + at least 1 discount |
| 22 | `PATCH /api/v1/offerings/{id}/publish` | 200 + status=PUBLISHED |
| 23 | `GET  /api/v1/public/offerings/{id}` | 200 + discountedPrice <= price, variants array |
| 24 | `GET  /api/v1/public/offerings/search` | 200 + paginated results |
| 25 | `GET  /api/v1/public/offerings/business/{ownerId}` | 200 + paginated content |
| 26 | `GET  /api/v1/public/offerings/category/{catId}` | 200 + at least 1 offering |
| 27 | `GET  /api/v1/public/offerings/featured` | 200 + paginated content |
| 28 | `GET  /api/v1/pricing/variants/{vid}` | 200 + basePrice, finalPrice |
| 29 | `PATCH .../variants/{vid}/inventory?quantity=75` | 200 + status=UPDATED |
| 30 | `PATCH .../offerings/{id}/inventory?quantity=200` | 200 + status=UPDATED |
| 31 | `GET  /api/v1/admin/offerings?status=PUBLISHED` | 200 + at least 1 offering |
| 32 | `PATCH /api/v1/offerings/{id}/archive` | 200 + status=ARCHIVED |
| 33 | `PATCH /api/v1/offerings/{id}/restore` | 200 + status=DRAFT |
| 34 | `PATCH /api/v1/admin/offerings/{id}/status` | 200 + admin force PUBLISHED |
| 35 | `PATCH /api/v1/discounts/{id}/deactivate` | 200 + status=INACTIVE |
| 36 | `PATCH .../variants/{vid}/deactivate` | 200 + status=INACTIVE |
| 37 | `DELETE /api/v1/variants/{vid}/attributes/{aid}` | 200 + status=DELETED |

## Flow Covered

1. **Admin setup** — login + create category hierarchy + define attributes
2. **Category management** — create root/child, get tree, update name
3. **Attribute management** — create dropdown attribute, list by category, update options
4. **Owner creates offering** — create in DRAFT, get details, update fields, list filtered
5. **Variant management** — create variant, update SKU/price, list paginated
6. **Attribute assignment** — assign predefined + custom attributes, list, update value
7. **Discount management** — create percentage discount, list by offering
8. **Publishing** — publish offering to make visible
9. **Public discovery** — get offering with pricing, search, browse by business/category/featured
10. **Pricing resolution** — verify variant pricing with discount applied
11. **Inventory updates** — update variant and offering quantities
12. **Admin oversight** — list all offerings filtered, admin force status change
13. **Lifecycle management** — archive, restore, admin re-publish
14. **Cleanup operations** — deactivate discount, deactivate variant, delete attribute

## Prerequisites

- Application running on `http://localhost:8080`
- Newman installed globally: `npm install -g newman`
- A seeded admin account (credentials in environment file)
- A seeded user account with an existing business (`owner_id` in environment)

## One-Click Run

```bash
bash src/test/resources/offering/e2e/scripts/run-offering-happy-path-newman.sh
```

Each run uses a timestamp-based `run_id` so fresh resources (category name, SKU, title) are created every time — no manual DB cleanup required.

## JSON Report

After each run a JSON report is saved to:
```
src/test/resources/offering/e2e/reports/offering-happy-path-<run_id>.json
```

## Customisation

| Variable | Default | Override |
|---|---|---|
| `base_url` | `http://localhost:8080` | Edit environment file |
| `admin_username` | `admin@example.com` | Edit environment file |
| `admin_password` | `admin-password` | Edit environment file |
| `user_username` | `user@example.com` | Edit environment file |
| `user_password` | `user-password` | Edit environment file |
| `owner_id` | `1` | Edit environment file |
| `run_id` | `$(date +%s)` | `--env-var run_id=my_value` |

## File Structure

```
src/test/resources/offering/e2e/
├── postman/
│   ├── Offering-Happy-Path.postman_collection.json   # 37-request collection
│   └── Offering-Happy-Path.postman_environment.json   # Environment variables
├── scripts/
│   └── run-offering-happy-path-newman.sh              # Newman runner script
├── reports/                                            # Auto-generated JSON reports
└── README.md                                           # This file
```
