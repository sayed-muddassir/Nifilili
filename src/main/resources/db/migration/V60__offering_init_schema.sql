CREATE TABLE "offering_categories"(
    "id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "parent_category_id" BIGINT
);
ALTER TABLE
    "offering_categories" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "offering_categories"."parent_category_id" IS 'Self referential for subcategories';
CREATE TABLE "offerings"(
    "id" BIGINT NOT NULL,
    "owner_type" VARCHAR(255) NOT NULL,
    "owner_id" BIGINT NOT NULL,
    "category_id" BIGINT NOT NULL,
    "type" VARCHAR(255) NOT NULL,
    "title" VARCHAR(255) NOT NULL,
    "description" TEXT NOT NULL,
    "sku" VARCHAR(255) NOT NULL,
    "price" DECIMAL(8, 2) NOT NULL,
    "is_dynamic_pricing" BOOLEAN NOT NULL,
    "available_quantity" INTEGER NOT NULL,
    "is_featured" BOOLEAN NOT NULL,
    "images" jsonb NOT NULL,
    "view_count" INTEGER NOT NULL,
    "is_b2b_enabled" BOOLEAN NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "created_by" BIGINT NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_by" BIGINT NOT NULL
);
ALTER TABLE
    "offerings" ADD PRIMARY KEY("id");
ALTER TABLE
    "offerings" ADD CONSTRAINT "offerings_sku_unique" UNIQUE("sku");
COMMENT
ON COLUMN
    "offerings"."owner_type" IS 'business, freelancer';
COMMENT
ON COLUMN
    "offerings"."owner_id" IS 'user id or business id based on owner type value';
COMMENT
ON COLUMN
    "offerings"."type" IS 'PRODUCT or SERVICE';
COMMENT
ON COLUMN
    "offerings"."sku" IS 'Optional SKU for non-variant offerings, unique per business';
COMMENT
ON COLUMN
    "offerings"."price" IS 'Base price, NULL for Dynamic Pricing (in case of service)';
COMMENT
ON COLUMN
    "offerings"."available_quantity" IS 'NULL for services or unlimited products';
COMMENT
ON COLUMN
    "offerings"."is_featured" IS 'If product is featured..we can implement like: 5 featured images can be selected from backend';
COMMENT
ON COLUMN
    "offerings"."images" IS 'Array of image urls';
COMMENT
ON COLUMN
    "offerings"."status" IS 'DRAFT, PUBLISHED, ARCHIVED';
CREATE TABLE "offering_variants"(
    "id" BIGINT NOT NULL,
    "offering_id" BIGINT NOT NULL,
    "sku" VARCHAR(255) NOT NULL,
    "price" DECIMAL(8, 2) NOT NULL,
    "available_quantity" BIGINT NOT NULL,
    "images" jsonb NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "created_by" BIGINT NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_by" BIGINT NOT NULL,
    "status" VARCHAR(255) NOT NULL
);
ALTER TABLE
    "offering_variants" ADD PRIMARY KEY("id");
ALTER TABLE
    "offering_variants" ADD CONSTRAINT "offering_variants_sku_unique" UNIQUE("sku");
COMMENT
ON COLUMN
    "offering_variants"."images" IS 'Variant specific images';
CREATE TABLE "offering_variant_attributes"(
    "id" BIGINT NOT NULL,
    "offering_variant_id" BIGINT NOT NULL,
    "offering_attributes_id" BIGINT NOT NULL,
    "attribute_name" VARCHAR(255) NOT NULL,
    "attribute_value" VARCHAR(255) NOT NULL
);
ALTER TABLE
    "offering_variant_attributes" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "offering_variant_attributes"."offering_attributes_id" IS 'NULL for custom attributes';
COMMENT
ON COLUMN
    "offering_variant_attributes"."attribute_name" IS 'Color, Size, Memory';
COMMENT
ON COLUMN
    "offering_variant_attributes"."attribute_value" IS 'RED, S, 64 GB';
CREATE TABLE "offering_discounts"(
    "id" BIGINT NOT NULL,
    "offering_id" BIGINT NOT NULL,
    "variant_id" BIGINT NOT NULL,
    "discount_type" VARCHAR(255) NOT NULL,
    "discount_value" BIGINT NOT NULL,
    "start_date" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "end_date" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "status" VARCHAR(255) NOT NULL
);
ALTER TABLE
    "offering_discounts" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "offering_discounts"."discount_type" IS 'Percentage / Fixed Amount';
CREATE TABLE "offering_attributes"(
    "id" BIGINT NOT NULL,
    "offering_category_id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "attribute_type" VARCHAR(255) NOT NULL,
    "options" jsonb NOT NULL
);
ALTER TABLE
    "offering_attributes" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "offering_attributes"."name" IS 'color, size, memory, etc';
COMMENT
ON COLUMN
    "offering_attributes"."attribute_type" IS 'text, number, etc';
COMMENT
ON COLUMN
    "offering_attributes"."options" IS 'for dropdowns: ["red","blue"] for color';