CREATE TABLE "orders"(
    "id" BIGINT NOT NULL,
    "user_id" BIGINT NOT NULL,
    "order_number" VARCHAR(255) NOT NULL,
    "invoice_number" VARCHAR(255) NOT NULL,
    "receiver_name" VARCHAR(255) NOT NULL,
    "contact_number" VARCHAR(255) NOT NULL,
    "email" VARCHAR(255) NOT NULL,
    "municipality_id" BIGINT NOT NULL,
    "ward_number" INTEGER NOT NULL,
    "tole_name" VARCHAR(255) NOT NULL,
    "address_field_1" TEXT NOT NULL,
    "postal_code" VARCHAR(255) NOT NULL,
    "subtotal_amount" DECIMAL(8, 2) NOT NULL,
    "delivery_charge" DECIMAL(8, 2) NOT NULL,
    "tax_amount" DECIMAL(8, 2) NOT NULL,
    "discount_amount" DECIMAL(8, 2) NOT NULL,
    "total_amount" DECIMAL(8, 2) NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "payment_status" VARCHAR(255) NOT NULL,
    "amount_paid" DECIMAL(8, 2) NOT NULL,
    "customer_notes" TEXT NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "created_by" BIGINT NOT NULL,
    "updated_by" BIGINT NOT NULL,
    "is_b2b_order" BOOLEAN NOT NULL,
    "b2b_quote_id" BIGINT NOT NULL,
    "source" VARCHAR(255) NOT NULL,
    "source_quote_id" BIGINT NOT NULL,
    "source_request_id" BIGINT NOT NULL
);
ALTER TABLE
    "orders" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "orders"."subtotal_amount" IS 'Sum of item subtotals';
COMMENT
ON COLUMN
    "orders"."delivery_charge" IS 'Sum of per-business delivery charges';
COMMENT
ON COLUMN
    "orders"."tax_amount" IS 'Sum of per-business taxes';
COMMENT
ON COLUMN
    "orders"."discount_amount" IS 'Sum of per-business discounts';
COMMENT
ON COLUMN
    "orders"."total_amount" IS 'Subtotal + delivery + tax - discount';
COMMENT
ON COLUMN
    "orders"."status" IS '''placed'', ''received'', ''processing'', ''shipped'', ''delivered'', ''rejected''';
COMMENT
ON COLUMN
    "orders"."customer_notes" IS 'Eg: gift wrap';
COMMENT
ON COLUMN
    "orders"."source" IS 'direct, b2b, quote, etc';
CREATE TABLE "order_items"(
    "id" BIGINT NOT NULL,
    "order_id" BIGINT NOT NULL,
    "business_id" BIGINT NOT NULL,
    "offering_id" BIGINT NOT NULL,
    "coupon_id" BIGINT NULL,
    "variant_id" BIGINT NULL,
    "title" VARCHAR(255) NOT NULL,
    "sku" VARCHAR(255) NOT NULL,
    "variant_attributes" jsonb NOT NULL,
    "quantity" INTEGER NOT NULL,
    "unit_price" DECIMAL(8, 2) NOT NULL,
    "discount_amount" DECIMAL(8, 2) NOT NULL,
    "delivery_charge" DECIMAL(8, 2) NOT NULL,
    "tax_amount" DECIMAL(8, 2) NOT NULL,
    "subtotal" DECIMAL(8, 2) NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "created_by" BIGINT NOT NULL,
    "updated_by" BIGINT NOT NULL
);
ALTER TABLE
    "order_items" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "order_items"."variant_id" IS 'null if no variant';
COMMENT
ON COLUMN
    "order_items"."title" IS 'Snapshot of offering title';
COMMENT
ON COLUMN
    "order_items"."sku" IS 'Snapshot of offering/variant SKU';
COMMENT
ON COLUMN
    "order_items"."variant_attributes" IS 'snapshot of attributes of variant';
COMMENT
ON COLUMN
    "order_items"."status" IS 'per item status -> placed, shipped, rejected';
CREATE TABLE "order_status_history"(
    "id" BIGINT NOT NULL,
    "order_item_id" BIGINT NOT NULL,
    "old_status" VARCHAR(255) NOT NULL,
    "new_status" VARCHAR(255) NOT NULL,
    "rejection_reason" TEXT NOT NULL,
    "created_by" BIGINT NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "order_status_history" ADD PRIMARY KEY("id");
CREATE TABLE "order_payments"(
    "id" BIGINT NOT NULL,
    "order_id" BIGINT NOT NULL,
    "payment_type_id" BIGINT NOT NULL,
    "amount" DECIMAL(8, 2) NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "payment_details" jsonb NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "created_by" BIGINT NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_by" BIGINT NOT NULL
);
ALTER TABLE
    "order_payments" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "order_payments"."payment_type_id" IS 'Payment type table will be there with payment types: cash on delivery, online, etc';
COMMENT
ON COLUMN
    "order_payments"."status" IS 'pending, pending_verification, verified, completed';
COMMENT
ON COLUMN
    "order_payments"."payment_details" IS 'different for different payment type.. if cod, then receiver name.. if online then transaction_id, gateway_transaction_id, etc';
CREATE TABLE "carts"(
    "id" BIGINT NOT NULL,
    "user_id" BIGINT NOT NULL
);
ALTER TABLE
    "carts" ADD PRIMARY KEY("id");
CREATE TABLE "cart_items"(
    "id" BIGINT NOT NULL,
    "cart_id" BIGINT NOT NULL,
    "offering_id" BIGINT NOT NULL,
    "variant_id" BIGINT NOT NULL,
    "quantity" INTEGER NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "created_by" BIGINT NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_by" BIGINT NOT NULL
);
ALTER TABLE
    "cart_items" ADD PRIMARY KEY("id");
CREATE TABLE "return_requests"(
    "id" BIGINT NOT NULL,
    "order_item_id" BIGINT NOT NULL,
    "reason" VARCHAR(255) NOT NULL,
    "reason_details" TEXT NOT NULL,
    "photos" jsonb NOT NULL,
    "pickup_address" jsonb NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "rma_number" VARCHAR(255) NULL,  -- currently without logistics or shipping integration, it would be null
    "requested_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "pickup_scheduled_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "received_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "inspected_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "rejected_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "rejection_reason" TEXT NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "created_by" BIGINT NOT NULL,
    "updated_by" BIGINT NOT NULL
);
ALTER TABLE
    "return_requests" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "return_requests"."reason" IS 'defective, wrong item, changed mind, etc';
COMMENT
ON COLUMN
    "return_requests"."pickup_address" IS 'Full address: {municipality_id, ward_number, tole_name, detailed_info, postal_code}';
COMMENT
ON COLUMN
    "return_requests"."status" IS 'requested, pickup_scheduled, picked_up, received, inspected, refunded, rejected';
COMMENT
ON COLUMN
    "return_requests"."rma_number" IS 'this is return merchandise authorization number.. currently without logistics or shipping integration, it would be null';
CREATE TABLE "order_refunds"(
    "id" BIGINT NOT NULL,
    "order_id" BIGINT NOT NULL,
    "refund_amount" DECIMAL(8, 2) NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "bank_name" VARCHAR(255) NOT NULL,
    "bank_account_name" VARCHAR(255) NOT NULL,
    "account_number" VARCHAR(255) NOT NULL,
    "branch" VARCHAR(255) NOT NULL,
    "refund_reference" VARCHAR(255) NOT NULL,
    "processed_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "order_refunds" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "order_refunds"."status" IS 'pending, approved, processed, failed, cancelled';
CREATE TABLE "refund_items"(
    "id" BIGINT NOT NULL,
    "refund_id" BIGINT NOT NULL,
    "return_request_id" BIGINT NOT NULL,
    "order_item_id" BIGINT NOT NULL,
    "refund_amount" BIGINT NOT NULL,
    "status" BIGINT NOT NULL
);
ALTER TABLE
    "refund_items" ADD PRIMARY KEY("id");