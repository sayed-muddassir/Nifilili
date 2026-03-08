CREATE TABLE "quote_requests"(
    "id" BIGINT NOT NULL,
    "offering_id" BIGINT NOT NULL,
    "user_id" BIGINT NOT NULL,
    "business_id" BIGINT NOT NULL,
    "request_number" VARCHAR(255) NOT NULL,
    "requirements" TEXT NOT NULL,
    "budget_range" VARCHAR(255) NULL,
    "preferred_timeline" DATE NULL,
    "delivery_address" jsonb NULL,
    "attachments" jsonb NULL,
    "status" VARCHAR(32) NOT NULL,
    "rejection_reason" TEXT NULL,
    "submitted_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "expired_at" TIMESTAMP(0) WITHOUT TIME ZONE NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "created_by" BIGINT NOT NULL,
    "updated_by" BIGINT NOT NULL
);
ALTER TABLE
    "quote_requests" ADD PRIMARY KEY("id");
CREATE UNIQUE INDEX "quote_requests_request_number_key"
    ON "quote_requests"("request_number");

CREATE TABLE "quotes"(
    "id" BIGINT NOT NULL,
    "request_id" BIGINT NOT NULL,
    "parent_quote_id" BIGINT NULL,
    "quote_number" VARCHAR(255) NOT NULL,
    "service_details" TEXT NOT NULL,
    "total_amount" DECIMAL(10, 2) NOT NULL,
    "currency" VARCHAR(16) NOT NULL,
    "estimated_duration_days" INTEGER NULL,
    "valid_until" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "attachments" jsonb NULL,
    "status" VARCHAR(32) NOT NULL,
    "sent_at" TIMESTAMP(0) WITHOUT TIME ZONE NULL,
    "accepted_at" TIMESTAMP(0) WITHOUT TIME ZONE NULL,
    "rejected_at" TIMESTAMP(0) WITHOUT TIME ZONE NULL,
    "rejection_reason" TEXT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "created_by" BIGINT NOT NULL,
    "updated_by" BIGINT NOT NULL
);
ALTER TABLE
    "quotes" ADD PRIMARY KEY("id");
CREATE UNIQUE INDEX "quotes_quote_number_key"
    ON "quotes"("quote_number");
CREATE INDEX "quotes_request_id_idx"
    ON "quotes"("request_id");
CREATE INDEX "quotes_parent_quote_id_idx"
    ON "quotes"("parent_quote_id");

CREATE TABLE "quote_line_items"(
    "id" BIGINT NOT NULL,
    "quote_id" BIGINT NOT NULL,
    "description" TEXT NOT NULL,
    "quantity" INTEGER NOT NULL,
    "unit_price" DECIMAL(10, 2) NOT NULL,
    "total_price" DECIMAL(10, 2) NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "created_by" BIGINT NOT NULL,
    "updated_by" BIGINT NOT NULL
);
ALTER TABLE
    "quote_line_items" ADD PRIMARY KEY("id");
CREATE INDEX "quote_line_items_quote_id_idx"
    ON "quote_line_items"("quote_id");

CREATE TABLE "quote_conversions"(
    "id" BIGINT NOT NULL,
    "quote_id" BIGINT NOT NULL,
    "order_id" BIGINT NOT NULL,
    "converted_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "converted_by" BIGINT NOT NULL
);
ALTER TABLE
    "quote_conversions" ADD PRIMARY KEY("id");
CREATE UNIQUE INDEX "quote_conversions_quote_id_key"
    ON "quote_conversions"("quote_id");
CREATE INDEX "quote_conversions_order_id_idx"
    ON "quote_conversions"("order_id");
