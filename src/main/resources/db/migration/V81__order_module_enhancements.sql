-- =============================================================
-- V23: Order module enhancements
-- Adds missing tables and fixes V11 column type/nullable issues
-- =============================================================

-- 1. payment_types — reference table for available payment methods
CREATE TABLE "payment_types" (
    "id"          BIGINT NOT NULL,
    "name"        VARCHAR(255) NOT NULL,
    "description" TEXT,
    "is_active"   BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at"  TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    "updated_at"  TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);
ALTER TABLE "payment_types" ADD PRIMARY KEY ("id");
ALTER TABLE "payment_types" ADD CONSTRAINT "uq_payment_types_name" UNIQUE ("name");

-- Seed default payment types
INSERT INTO "payment_types" ("id", "name", "description", "is_active", "created_at", "updated_at")
VALUES
    (1, 'CASH_ON_DELIVERY', 'Payment collected at the time of delivery', TRUE, NOW(), NOW()),
    (2, 'MANUAL_BANK_TRANSFER', 'Customer transfers payment manually and submits proof', TRUE, NOW(), NOW());

-- 2-4. business_configurations, coupons, and coupon_usage now belong to the
-- consolidated business schema in V20. Order still adds the post-order FK.
ALTER TABLE "coupon_usage"
    ADD CONSTRAINT "fk_coupon_usage_order" FOREIGN KEY ("order_id") REFERENCES "orders" ("id");

-- 5. cancellation_requests — per-item cancellation tracking
CREATE TABLE "cancellation_requests" (
    "id"              BIGINT NOT NULL,
    "order_item_id"   BIGINT NOT NULL,
    "reason"          VARCHAR(255) NOT NULL,
    "note"            TEXT,
    "status"          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    "decided_by"      BIGINT,
    "decided_at"      TIMESTAMP(0) WITHOUT TIME ZONE,
    "decision_reason" TEXT,
    "created_at"      TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    "created_by"      BIGINT NOT NULL
);
ALTER TABLE "cancellation_requests" ADD PRIMARY KEY ("id");
ALTER TABLE "cancellation_requests"
    ADD CONSTRAINT "fk_cancel_req_order_item" FOREIGN KEY ("order_item_id") REFERENCES "order_items" ("id");

-- 6. payment_status_history — tracks payment status transitions
CREATE TABLE "payment_status_history" (
    "id"         BIGINT NOT NULL,
    "payment_id" BIGINT NOT NULL,
    "old_status" VARCHAR(255) NOT NULL,
    "new_status" VARCHAR(255) NOT NULL,
    "note"       TEXT,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    "created_by" BIGINT
);
ALTER TABLE "payment_status_history" ADD PRIMARY KEY ("id");
ALTER TABLE "payment_status_history"
    ADD CONSTRAINT "fk_pay_hist_payment" FOREIGN KEY ("payment_id") REFERENCES "order_payments" ("id");

-- =============================================================
-- Fix V11 column type issues
-- =============================================================

-- refund_items: fix column types (BIGINT → proper types)
ALTER TABLE "refund_items" ALTER COLUMN "refund_amount" TYPE DECIMAL(8, 2);
ALTER TABLE "refund_items" ALTER COLUMN "status" TYPE VARCHAR(255) USING status::VARCHAR(255);

-- orders: make optional columns nullable
ALTER TABLE "orders" ALTER COLUMN "customer_notes" DROP NOT NULL;
ALTER TABLE "orders" ALTER COLUMN "is_b2b_order" SET DEFAULT FALSE;
ALTER TABLE "orders" ALTER COLUMN "b2b_quote_id" DROP NOT NULL;
ALTER TABLE "orders" ALTER COLUMN "source" DROP NOT NULL;
ALTER TABLE "orders" ALTER COLUMN "source_quote_id" DROP NOT NULL;
ALTER TABLE "orders" ALTER COLUMN "source_request_id" DROP NOT NULL;

-- return_requests: make lifecycle timestamps nullable (set when phase is reached)
ALTER TABLE "return_requests" ALTER COLUMN "reason_details" DROP NOT NULL;
ALTER TABLE "return_requests" ALTER COLUMN "pickup_scheduled_at" DROP NOT NULL;
ALTER TABLE "return_requests" ALTER COLUMN "received_at" DROP NOT NULL;
ALTER TABLE "return_requests" ALTER COLUMN "inspected_at" DROP NOT NULL;
ALTER TABLE "return_requests" ALTER COLUMN "rejected_at" DROP NOT NULL;
ALTER TABLE "return_requests" ALTER COLUMN "rejection_reason" DROP NOT NULL;

-- order_refunds: make post-processing fields nullable
ALTER TABLE "order_refunds" ALTER COLUMN "refund_reference" DROP NOT NULL;
ALTER TABLE "order_refunds" ALTER COLUMN "processed_at" DROP NOT NULL;

-- order_status_history: rejection_reason is only set on rejections
ALTER TABLE "order_status_history" ALTER COLUMN "rejection_reason" DROP NOT NULL;

-- cart_items: variant_id should be nullable (products without variants)
ALTER TABLE "cart_items" ALTER COLUMN "variant_id" DROP NOT NULL;

-- order_items: variant_attributes should be nullable
ALTER TABLE "order_items" ALTER COLUMN "variant_attributes" DROP NOT NULL;
ALTER TABLE "order_items" ALTER COLUMN "sku" DROP NOT NULL;
