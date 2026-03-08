-- Gap 18: Change SKU uniqueness from global to per-business/per-offering

-- Offerings: SKU unique within same owner (not globally)
ALTER TABLE offerings DROP CONSTRAINT IF EXISTS offerings_sku_unique;
ALTER TABLE offerings ADD CONSTRAINT offerings_owner_sku_unique UNIQUE (owner_id, sku);

-- Variants: SKU unique within same offering (not globally)
ALTER TABLE offering_variants DROP CONSTRAINT IF EXISTS offering_variants_sku_unique;
ALTER TABLE offering_variants ADD CONSTRAINT offering_variants_offering_sku_unique UNIQUE (offering_id, sku);

-- Gap 20: Allow NULL offering_attributes_id for custom attributes
ALTER TABLE offering_variant_attributes ALTER COLUMN offering_attributes_id DROP NOT NULL;
