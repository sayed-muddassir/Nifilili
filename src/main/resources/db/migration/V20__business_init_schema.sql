CREATE TABLE "business_verticals" (
    "id" BIGINT PRIMARY KEY,
    "name" VARCHAR(255) NOT NULL,
    "slug" VARCHAR(255) NOT NULL,
    "description" TEXT NOT NULL,
    "icon_url" VARCHAR(255) NOT NULL,
    "is_active" BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE "province_master" (
    "id" BIGINT PRIMARY KEY,
    "name" VARCHAR(255) NOT NULL
);

CREATE TABLE "district_master" (
    "id" BIGINT PRIMARY KEY,
    "province_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL
);

CREATE TABLE "municipality_master" (
    "id" BIGINT PRIMARY KEY,
    "district_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "type" VARCHAR(255) NOT NULL
);

CREATE TABLE "categories" (
    "id" BIGINT PRIMARY KEY,
    "business_vertical_id" BIGINT NOT NULL,
    "parent_category_id" BIGINT,
    "name" VARCHAR(255) NOT NULL,
    "slug" VARCHAR(255) NOT NULL,
    "description" TEXT NOT NULL,
    "icon_url" VARCHAR(255) NOT NULL,
    "active_status" BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT "fk_categories_vertical"
        FOREIGN KEY ("business_vertical_id") REFERENCES "business_verticals" ("id")
);

CREATE TABLE "business_master" (
    "id" BIGINT PRIMARY KEY,
    "vertical_id" BIGINT NOT NULL,
    "owner_user_id" BIGINT,
    "claimed_by_user_id" BIGINT,
    "name" VARCHAR(255) NOT NULL,
    "legal_name" VARCHAR(255),
    "municipality_id" BIGINT NOT NULL,
    "ward_number" BIGINT NOT NULL,
    "tole_name" VARCHAR(255) NOT NULL,
    "address_field_1" VARCHAR(255) NOT NULL,
    "address_field_2" VARCHAR(255),
    "postal_code" VARCHAR(255),
    "latitude" DECIMAL(19, 7),
    "longitude" DECIMAL(19, 7),
    "contacts" jsonb,
    "business_hours" jsonb,
    "website" VARCHAR(255),
    "registration_date" DATE NOT NULL,
    "business_summary" TEXT,
    "source" VARCHAR(255) NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "is_claimed" BOOLEAN NOT NULL DEFAULT FALSE,
    "is_kyc_verified" BOOLEAN NOT NULL DEFAULT FALSE,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "average_rating" DECIMAL(8, 2) NOT NULL,
    "review_count" INTEGER NOT NULL,
    CONSTRAINT "fk_business_master_vertical"
        FOREIGN KEY ("vertical_id") REFERENCES "business_verticals" ("id"),
    CONSTRAINT "fk_business_master_municipality"
        FOREIGN KEY ("municipality_id") REFERENCES "municipality_master" ("id"),
    CONSTRAINT "fk_business_master_owner_user"
        FOREIGN KEY ("owner_user_id") REFERENCES "users" ("id"),
    CONSTRAINT "fk_business_master_claimed_by_user"
        FOREIGN KEY ("claimed_by_user_id") REFERENCES "users" ("id")
);

CREATE TABLE "sections" (
    "id" BIGINT PRIMARY KEY,
    "vertical_id" BIGINT NOT NULL,
    "category_id" BIGINT,
    "name" VARCHAR(255) NOT NULL,
    "label" VARCHAR(255) NOT NULL,
    "prompt" VARCHAR(255),
    "required" BOOLEAN NOT NULL,
    "allow_multiple" BOOLEAN NOT NULL,
    "groupable" BOOLEAN NOT NULL,
    CONSTRAINT "fk_sections_category"
        FOREIGN KEY ("category_id") REFERENCES "categories" ("id")
);

CREATE TABLE "section_fields" (
    "id" BIGINT PRIMARY KEY,
    "section_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "label" VARCHAR(255) NOT NULL,
    "type" VARCHAR(255) NOT NULL,
    "options" jsonb NOT NULL,
    "required" BOOLEAN NOT NULL,
    "allow_multiple" BOOLEAN NOT NULL,
    CONSTRAINT "fk_section_fields_section"
        FOREIGN KEY ("section_id") REFERENCES "sections" ("id")
);

CREATE TABLE "section_groups" (
    "id" BIGINT PRIMARY KEY,
    "business_id" BIGINT NOT NULL,
    "section_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    CONSTRAINT "fk_section_groups_business"
        FOREIGN KEY ("business_id") REFERENCES "business_master" ("id"),
    CONSTRAINT "fk_section_groups_section"
        FOREIGN KEY ("section_id") REFERENCES "sections" ("id")
);

CREATE TABLE "business_data" (
    "id" BIGINT PRIMARY KEY,
    "business_id" BIGINT NOT NULL,
    "section_id" BIGINT NOT NULL,
    "section_group_id" BIGINT,
    "field_values" jsonb NOT NULL,
    CONSTRAINT "fk_business_data_business"
        FOREIGN KEY ("business_id") REFERENCES "business_master" ("id"),
    CONSTRAINT "fk_business_data_section"
        FOREIGN KEY ("section_id") REFERENCES "sections" ("id"),
    CONSTRAINT "fk_business_data_section_group"
        FOREIGN KEY ("section_group_id") REFERENCES "section_groups" ("id")
);

CREATE TABLE "business_category_mapping" (
    "id" BIGINT PRIMARY KEY,
    "business_id" BIGINT NOT NULL,
    "category_id" BIGINT NOT NULL,
    CONSTRAINT "uq_business_category_mapping" UNIQUE ("business_id", "category_id"),
    CONSTRAINT "fk_business_category_business"
        FOREIGN KEY ("business_id") REFERENCES "business_master" ("id"),
    CONSTRAINT "fk_business_category_category"
        FOREIGN KEY ("category_id") REFERENCES "categories" ("id")
);

CREATE TABLE "document_definitions" (
    "id" BIGINT PRIMARY KEY,
    "vertical_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "label" VARCHAR(255) NOT NULL,
    "allowed_extensions" jsonb NOT NULL,
    "max_file_size" INTEGER NOT NULL,
    "required" BOOLEAN NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT "fk_document_definitions_vertical"
        FOREIGN KEY ("vertical_id") REFERENCES "business_verticals" ("id")
);

CREATE TABLE "business_documents" (
    "id" BIGINT PRIMARY KEY,
    "business_id" BIGINT NOT NULL,
    "document_definition_id" BIGINT NOT NULL,
    "file_url" VARCHAR(255) NOT NULL,
    "file_name" VARCHAR(255) NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "rejection_reason" TEXT,
    "reviewed_at" TIMESTAMP(0) WITHOUT TIME ZONE,
    "reviewed_by_user_id" BIGINT,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT "fk_business_documents_business"
        FOREIGN KEY ("business_id") REFERENCES "business_master" ("id"),
    CONSTRAINT "fk_business_documents_definition"
        FOREIGN KEY ("document_definition_id") REFERENCES "document_definitions" ("id"),
    CONSTRAINT "fk_business_documents_reviewed_by"
        FOREIGN KEY ("reviewed_by_user_id") REFERENCES "users" ("id")
);

CREATE TABLE "business_kyc" (
    "id" BIGINT PRIMARY KEY,
    "business_id" BIGINT NOT NULL,
    "kyc_status" VARCHAR(255) NOT NULL,
    "admin_message" TEXT NOT NULL,
    "rejection_reason" TEXT,
    "submission_count" INTEGER NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT "fk_business_kyc_business"
        FOREIGN KEY ("business_id") REFERENCES "business_master" ("id")
);

CREATE TABLE "business_kyc_history" (
    "id" BIGINT PRIMARY KEY,
    "business_id" BIGINT NOT NULL,
    "kyc_status" VARCHAR(255) NOT NULL,
    "admin_message" TEXT NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT "fk_business_kyc_history_business"
        FOREIGN KEY ("business_id") REFERENCES "business_master" ("id")
);

CREATE TABLE "attribute_definitions" (
    "id" BIGINT PRIMARY KEY,
    "vertical_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "label" VARCHAR(255) NOT NULL,
    "type" VARCHAR(255) NOT NULL,
    "options" jsonb NOT NULL,
    "required" BOOLEAN NOT NULL,
    "allow_multiple" BOOLEAN NOT NULL,
    "prompt" TEXT NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT "fk_attribute_definitions_vertical"
        FOREIGN KEY ("vertical_id") REFERENCES "business_verticals" ("id")
);

CREATE TABLE "business_attributes" (
    "id" BIGINT PRIMARY KEY,
    "business_id" BIGINT NOT NULL,
    "attribute_id" BIGINT NOT NULL,
    "attribute_value" jsonb NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT "fk_business_attributes_business"
        FOREIGN KEY ("business_id") REFERENCES "business_master" ("id"),
    CONSTRAINT "fk_business_attributes_definition"
        FOREIGN KEY ("attribute_id") REFERENCES "attribute_definitions" ("id")
);

CREATE TABLE "business_configurations" (
    "id" BIGINT PRIMARY KEY,
    "business_id" BIGINT NOT NULL,
    "config_key" VARCHAR(255) NOT NULL,
    "config_value" VARCHAR(255) NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    "created_by" BIGINT,
    "updated_by" BIGINT,
    CONSTRAINT "uq_biz_config_key" UNIQUE ("business_id", "config_key"),
    CONSTRAINT "fk_business_configurations_business"
        FOREIGN KEY ("business_id") REFERENCES "business_master" ("id")
);

CREATE TABLE "coupons" (
    "id" BIGINT PRIMARY KEY,
    "business_id" BIGINT NOT NULL,
    "code" VARCHAR(255) NOT NULL,
    "discount_type" VARCHAR(50) NOT NULL,
    "discount_value" DECIMAL(8, 2) NOT NULL,
    "max_discount" DECIMAL(8, 2),
    "min_order_amount" DECIMAL(8, 2),
    "valid_from" DATE NOT NULL,
    "valid_to" DATE NOT NULL,
    "is_active" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    "created_by" BIGINT,
    "updated_by" BIGINT,
    CONSTRAINT "uq_coupon_biz_code" UNIQUE ("business_id", "code"),
    CONSTRAINT "fk_coupons_business"
        FOREIGN KEY ("business_id") REFERENCES "business_master" ("id")
);

CREATE TABLE "coupon_usage" (
    "id" BIGINT PRIMARY KEY,
    "coupon_id" BIGINT NOT NULL,
    "user_id" BIGINT NOT NULL,
    "order_id" BIGINT NOT NULL,
    "used_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT "fk_coupon_usage_coupon"
        FOREIGN KEY ("coupon_id") REFERENCES "coupons" ("id")
);

COMMENT ON COLUMN "business_master"."contacts" IS '{"phone": ["123-456"], "email": "info@business.com"}';
COMMENT ON COLUMN "business_master"."business_hours" IS '{"monday": {"open": "09:00", "close": "17:00"}}';
COMMENT ON COLUMN "business_master"."source" IS 'business can be added in bulk by admin, or by end user';
COMMENT ON COLUMN "business_master"."status" IS 'pending, published, unpublished';
COMMENT ON COLUMN "sections"."prompt" IS 'Prompt for businesses to add this missing section like: Add courses, etc';
COMMENT ON COLUMN "section_fields"."type" IS 'Text, number, dropdown, checkbox, media_url';
COMMENT ON COLUMN "section_fields"."options" IS 'Stores dropdown/checkbox options eg: ["Veg", "Non-Veg"]';
COMMENT ON COLUMN "municipality_master"."type" IS 'rural municipality or municipality';
COMMENT ON COLUMN "business_documents"."status" IS 'pending, approved, rejected';
COMMENT ON COLUMN "attribute_definitions"."type" IS 'Text, number, dropdown, checkbox, boolean';
COMMENT ON COLUMN "attribute_definitions"."options" IS '["Yes", "No"]';
COMMENT ON COLUMN "attribute_definitions"."allow_multiple" IS 'For multi-value attributes like: facilities';
COMMENT ON COLUMN "business_configurations"."config_key" IS 'delivery_charge, tax_rate';
COMMENT ON COLUMN "business_configurations"."config_value" IS '''500.00'' for delivery, ''13.00'' for tax, etc';
COMMENT ON COLUMN "coupons"."discount_type" IS '''Percentage'' or ''Fixed''';

CREATE INDEX "idx_business_master_status" ON "business_master" ("status");
CREATE INDEX "idx_business_master_vertical_id" ON "business_master" ("vertical_id");
CREATE INDEX "idx_business_master_owner_user_id" ON "business_master" ("owner_user_id");
CREATE INDEX "idx_business_master_source_claim_status" ON "business_master" ("source", "is_claimed", "status");
CREATE INDEX "idx_sections_vertical_category" ON "sections" ("vertical_id", "category_id");
CREATE INDEX "idx_business_data_business_section" ON "business_data" ("business_id", "section_id");
CREATE INDEX "idx_business_data_business_section_group" ON "business_data" ("business_id", "section_id", "section_group_id");
CREATE INDEX "idx_business_kyc_status" ON "business_kyc" ("kyc_status");
CREATE INDEX "idx_business_documents_business_status" ON "business_documents" ("business_id", "status");

CREATE OR REPLACE FUNCTION validate_section_category_vertical_match()
RETURNS TRIGGER AS $$
DECLARE
    category_vertical_id BIGINT;
BEGIN
    IF NEW.category_id IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT business_vertical_id
    INTO category_vertical_id
    FROM categories
    WHERE id = NEW.category_id;

    IF category_vertical_id IS NULL THEN
        RAISE EXCEPTION 'Category % does not exist', NEW.category_id;
    END IF;

    IF category_vertical_id <> NEW.vertical_id THEN
        RAISE EXCEPTION 'Section vertical_id % must match category vertical_id %', NEW.vertical_id, category_vertical_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sections_validate_category_vertical
BEFORE INSERT OR UPDATE ON sections
FOR EACH ROW
EXECUTE FUNCTION validate_section_category_vertical_match();
