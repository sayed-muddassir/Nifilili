# Order Happy Path E2E Tests

End-to-end tests for the order module using Postman + Newman.

## What Is Tested

| Step | Request | Expectation |
|---|---|---|
| 01 | `POST /api/v1/auth/login` (user) | 200 + user token |
| 02 | `POST /api/v1/auth/login` (owner) | 200 + owner token |
| 03 | `POST /api/v1/auth/login` (admin) | 200 + admin token |
| 04 | `POST /api/v1/cart/items` | 200 + cart with items |
| 05 | `GET  /api/v1/cart` | 200 + cartId, items array, summary |
| 06 | `PUT  /api/v1/cart/items/{id}` | 200 + updated quantity |
| 07 | `POST /api/v1/checkout/preview` | 200 + business groups, summary with payableAmount |
| 08 | `POST /api/v1/orders` | 201 + orderId, orderNumber, paymentStatus |
| 09 | `GET  /api/v1/orders` | 200 + paginated order list |
| 10 | `GET  /api/v1/orders/{id}` | 200 + full details with items, payment, timeline |
| 11 | `PUT  /api/v1/business/order-items/{id}/status` (RECEIVED) | 200 |
| 12 | `PUT  /api/v1/business/order-items/{id}/status` (PREPARING) | 200 |
| 13 | `PUT  /api/v1/business/order-items/{id}/status` (SHIPPED) | 200 |
| 14 | `PUT  /api/v1/business/order-items/{id}/status` (DELIVERED) | 200 |
| 15 | `GET  /api/v1/business/orders/{id}/payment` | 200 + payment status |
| 16 | `PUT  /api/v1/business/orders/{id}/payment/cod` | 200 + COD recorded |
| 17 | `POST /api/v1/order-items/{id}/returns` | 201 + rmaNumber |
| 18 | `PUT  /api/v1/business/returns/{id}/status` (PICKUP_SCHEDULED) | 200 |
| 19 | `PUT  /api/v1/business/returns/{id}/status` (PICKED_UP) | 200 |
| 20 | `PUT  /api/v1/business/returns/{id}/status` (RECEIVED) | 200 |
| 21 | `PUT  /api/v1/business/returns/{id}/status` (INSPECTED) | 200 |
| 22 | `PUT  /api/v1/business/returns/{id}/status` (REFUNDED) | 200 |
| 23 | `POST /api/v1/orders/{id}/refunds` | 201 + refundId |
| 24 | `PUT  /api/v1/business/orders/{id}/payment/refunds/{rid}/status` (APPROVED) | 200 |
| 25 | `PUT  /api/v1/business/orders/{id}/payment/refunds/{rid}/status` (PROCESSED) | 200 |
| 26 | `POST /api/v1/business/coupons` | 201 + coupon ID |
| 27 | `GET  /api/v1/business/coupons` | 200 + paginated coupons |
| 28 | `DELETE /api/v1/business/coupons/{id}` | 204 |
| 29 | `PUT  /api/v1/business/config` | 200 + config key/value |
| 30 | `GET  /api/v1/business/config` | 200 + config array |
| 31 | `GET  /api/v1/admin/payment-types` | 200 + payment types array |

## Flow Covered

1. **Authentication** — login as user, owner, admin to obtain JWT tokens
2. **Cart management** — add item, view cart, update quantity
3. **Checkout** — preview with address, view pricing breakdown by business
4. **Order placement** — place COD order, verify order number and payment status
5. **Order queries** — list user orders, get full order details with items and timeline
6. **Business fulfillment** — status transitions: RECEIVED -> PREPARING -> SHIPPED -> DELIVERED
7. **Payment recording** — get payment status, record COD collection
8. **Return lifecycle** — create return request, full lifecycle: PICKUP_SCHEDULED -> PICKED_UP -> RECEIVED -> INSPECTED -> REFUNDED
9. **Refund processing** — create refund with bank details, approve, process
10. **Coupon management** — create, list, deactivate coupons
11. **Business configuration** — upsert and read config key-value pairs
12. **Admin oversight** — list available payment types

## Prerequisites

- Application running on `http://localhost:8080`
- Newman installed globally: `npm install -g newman`
- A seeded admin account (credentials in environment file)
- A seeded user account (credentials in environment file)
- A seeded owner account with an existing business (credentials in environment file)
- At least one offering in the database for cart operations

## One-Click Run

```bash
bash src/test/resources/order/e2e/scripts/run-order-happy-path-newman.sh
```

Each run uses a timestamp-based `run_id` so fresh resources are created every time — no manual DB cleanup required.

## JSON Report

After each run a JSON report is saved to:
```
src/test/resources/order/e2e/reports/order-happy-path-<run_id>.json
```

## Customisation

| Variable | Default | Override |
|---|---|---|
| `base_url` | `http://localhost:8080` | Edit environment file |
| `user_email` | `testuser@example.com` | Edit environment file |
| `user_password` | `user123` | Edit environment file |
| `owner_email` | `owner@example.com` | Edit environment file |
| `owner_password` | `owner123` | Edit environment file |
| `admin_email` | `admin@nifilili.com` | Edit environment file |
| `admin_password` | `admin123` | Edit environment file |
| `payment_type_id` | `1` | Edit environment file |
| `run_id` | `$(date +%s)` | `--env-var run_id=my_value` |

## File Structure

```
src/test/resources/order/e2e/
├── postman/
│   ├── Order-Happy-Path.postman_collection.json   # 31-request collection
│   └── Order-Happy-Path.postman_environment.json   # Environment variables
├── scripts/
│   └── run-order-happy-path-newman.sh              # Newman runner script
├── reports/                                          # Auto-generated JSON reports
└── README.md                                         # This file
```
