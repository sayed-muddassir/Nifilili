CREATE TABLE "attribute_definitions"(
    "id" BIGINT NOT NULL,
    "vertical_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "label" VARCHAR(255) NOT NULL,
    "type" VARCHAR(255) NOT NULL,
    "options" jsonb NOT NULL,
    "required" BOOLEAN NOT NULL,
    "allow_multiple" BOOLEAN NOT NULL,
    "prompt" TEXT NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "attribute_definitions" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "attribute_definitions"."type" IS 'Text, number, dropdown, checkbox, boolean';
COMMENT
ON COLUMN
    "attribute_definitions"."options" IS '["Yes", "No"]';
COMMENT
ON COLUMN
    "attribute_definitions"."allow_multiple" IS 'For multi-value attributes like: facilities';
CREATE TABLE "business_attributes"(
    "id" BIGINT NOT NULL,
    "business_id" BIGINT NOT NULL,
    "attribute_id" BIGINT NOT NULL,
    "attribute_value" jsonb NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "business_attributes" ADD PRIMARY KEY("id");
-- Added in V23
--CREATE TABLE "business_configurations"(
--    "id" BIGINT NOT NULL,
--    "business_id" BIGINT NOT NULL,
--    "config_key" VARCHAR(255) NOT NULL,
--    "config_value" VARCHAR(255) NOT NULL,
--    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
--    "created_by" BIGINT NOT NULL,
--    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
--    "updated_by" BIGINT NOT NULL
--);
--ALTER TABLE
--    "business_configurations" ADD PRIMARY KEY("id");
--COMMENT
--ON COLUMN
--    "business_configurations"."config_key" IS 'delivery_charge, tax_rate';
--COMMENT
--ON COLUMN
--    "business_configurations"."config_value" IS '''500.00'' for delivery, ''13.00'' for tax, etc';
--CREATE TABLE "coupons"(
--    "id" BIGINT NOT NULL,
--    "business_id" BIGINT NOT NULL,
--    "code" VARCHAR(255) NOT NULL,
--    "description" VARCHAR(255) NOT NULL,
--    "discount_type" VARCHAR(255) NOT NULL,
--    "discount_value" DECIMAL(8, 2) NOT NULL,
--    "max_discount_amount" DECIMAL(8, 2) NOT NULL,
--    "min_order_amount" DECIMAL(8, 2) NOT NULL,
--    "max_usage_count" INTEGER NOT NULL,
--    "usage_count" INTEGER NOT NULL,
--    "max_usage_per_user" INTEGER NOT NULL,
--    "status" VARCHAR(255) NOT NULL,
--    "start_date" TIMESTAMP(0) WITH
--        TIME zone NOT NULL,
--        "expiry_date" TIMESTAMP(0)
--    WITH
--        TIME zone NOT NULL,
--        "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
--        "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
--        "created_by" BIGINT NOT NULL,
--        "updated_by" BIGINT NOT NULL
--);
--ALTER TABLE
--    "coupons" ADD PRIMARY KEY("id");
--COMMENT
--ON COLUMN
--    "coupons"."description" IS 'Dashain Sale 2025';
--COMMENT
--ON COLUMN
--    "coupons"."discount_type" IS '''Percentage'' or ''Fixed''';
--CREATE TABLE "coupon_usage"(
--    "id" BIGINT NOT NULL,
--    "coupon_id" BIGINT NOT NULL,
--    "user_id" BIGINT NOT NULL,
--    "order_id" BIGINT NOT NULL,
--    "order_item_id" BIGINT NOT NULL,
--    "usage_date" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
--);
--ALTER TABLE
--    "coupon_usage" ADD PRIMARY KEY("id");