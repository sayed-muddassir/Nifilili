CREATE TABLE "business_verticals"(
    "id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "slug" VARCHAR(255) NOT NULL,
    "description" TEXT NOT NULL,
    "icon_url" VARCHAR(255) NOT NULL,
    "is_active" BIGINT NOT NULL
);
ALTER TABLE
    "business_verticals" ADD PRIMARY KEY("id");
CREATE TABLE "categories"(
    "id" BIGINT NOT NULL,
    "business_vertical_id" BIGINT NOT NULL,
    "parent_category_id" BIGINT NULL,
    "name" VARCHAR(255) NOT NULL,
    "slug" VARCHAR(255) NOT NULL,
    "description" TEXT NOT NULL,
    "icon_url" VARCHAR(255) NOT NULL,
    "active_status" BIGINT NOT NULL
);
ALTER TABLE
    "categories" ADD PRIMARY KEY("id");
CREATE TABLE "business_master"(
    "id" BIGINT NOT NULL,
    "vertical_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "municipality_id" BIGINT NOT NULL,
    "ward_number" BIGINT NOT NULL,
    "tole_name" VARCHAR(255) NOT NULL,
    "address_field_1" VARCHAR(255) NOT NULL,
    "postal_code" VARCHAR(255) NOT NULL,
    "latitude" BIGINT NOT NULL,
    "longitude" BIGINT NOT NULL,
    "contacts" jsonb NOT NULL,
    "business_hours" jsonb NOT NULL,
    "website" VARCHAR(255) NOT NULL,
    "registration_date" DATE NOT NULL,
    "business_summary" TEXT NOT NULL,
    "source" VARCHAR(255) NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "is_claimed" BIGINT NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "average_rating" DECIMAL(8, 2) NOT NULL,
    "review_count" INTEGER NOT NULL
);
ALTER TABLE
    "business_master" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "business_master"."contacts" IS '{"phone": ["123-456"], "email": "info@business.com"}';
COMMENT
ON COLUMN
    "business_master"."business_hours" IS '{"monday": {"open": "09:00", "close": "17:00"}}';
COMMENT
ON COLUMN
    "business_master"."source" IS 'business can be added in bulk by admin, or by end user';
COMMENT
ON COLUMN
    "business_master"."status" IS 'pending, published, unpublished';
CREATE TABLE "sections"(
    "id" BIGINT NOT NULL,
    "vertical_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "label" VARCHAR(255) NOT NULL,
    "prompt" VARCHAR(255) NOT NULL,
    "required" BOOLEAN NOT NULL,
    "allow_multiple" BOOLEAN NOT NULL,
    "groupable" BOOLEAN NOT NULL
);
ALTER TABLE
    "sections" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "sections"."prompt" IS 'Prompt for businesses to add this missing section like: Add courses, etc';
CREATE TABLE "section_fields"(
    "id" BIGINT NOT NULL,
    "section_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "label" VARCHAR(255) NOT NULL,
    "type" VARCHAR(255) NOT NULL,
    "options" jsonb NOT NULL,
    "required" BOOLEAN NOT NULL,
    "allow_multiple" BOOLEAN NOT NULL
);
ALTER TABLE
    "section_fields" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "section_fields"."type" IS 'Text, number, dropdown, checkbox, media_url';
COMMENT
ON COLUMN
    "section_fields"."options" IS 'Stores dropdown/checkbox options eg: ["Veg", "Non-Veg"]';
CREATE TABLE "business_data"(
    "id" BIGINT NOT NULL,
    "business_id" BIGINT NOT NULL,
    "section_id" BIGINT NOT NULL,
    "section_group_id" BIGINT NOT NULL,
    "field_values" jsonb NOT NULL
);
ALTER TABLE
    "business_data" ADD PRIMARY KEY("id");
CREATE TABLE "business_category_mapping"(
    "id" BIGINT NOT NULL,
    "business_id" BIGINT NOT NULL,
    "category_id" BIGINT NOT NULL
);
ALTER TABLE
    "business_category_mapping" ADD PRIMARY KEY("id");
CREATE TABLE "province_master"(
    "id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL
);
ALTER TABLE
    "province_master" ADD PRIMARY KEY("id");
CREATE TABLE "district_master"(
    "id" BIGINT NOT NULL,
    "province_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL
);
ALTER TABLE
    "district_master" ADD PRIMARY KEY("id");
CREATE TABLE "municipality_master"(
    "id" BIGINT NOT NULL,
    "district_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "type" VARCHAR(255) NOT NULL
);
ALTER TABLE
    "municipality_master" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "municipality_master"."type" IS 'rural municipality or municipality';
CREATE TABLE "section_groups"(
    "id" BIGINT NOT NULL,
    "business_id" BIGINT NOT NULL,
    "section_id" BIGINT NOT NULL,
    "name" BIGINT NOT NULL
);
ALTER TABLE
    "section_groups" ADD PRIMARY KEY("id");
CREATE TABLE "business_kyc"(
    "id" BIGINT NOT NULL,
    "business_id" BIGINT NOT NULL,
    "kyc_status" VARCHAR(255) NOT NULL,
    "admin_message" TEXT NOT NULL,
    "submission_count" INTEGER NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "business_kyc" ADD PRIMARY KEY("id");
CREATE TABLE "document_definitions"(
    "id" BIGINT NOT NULL,
    "vertical_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "label" VARCHAR(255) NOT NULL,
    "allowed_extensions" jsonb NOT NULL,
    "max_file_size" INTEGER NOT NULL,
    "required" BOOLEAN NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "document_definitions" ADD PRIMARY KEY("id");
CREATE TABLE "business_documents"(
    "id" BIGINT NOT NULL,
    "business_id" BIGINT NOT NULL,
    "document_definition_id" BIGINT NOT NULL,
    "file_url" VARCHAR(255) NOT NULL,
    "file_name" VARCHAR(255) NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "rejection_reason" TEXT NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "business_documents" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "business_documents"."status" IS 'pending, approved, rejected';
CREATE TABLE "business_kyc_history"(
    "id" BIGINT NOT NULL,
    "business_id" BIGINT NOT NULL,
    "kyc_status" VARCHAR(255) NOT NULL,
    "admin_message" TEXT NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "business_kyc_history" ADD PRIMARY KEY("id");