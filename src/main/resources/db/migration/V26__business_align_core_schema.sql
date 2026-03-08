-- Align core business table with ownership, richer address, and coordinate precision.

ALTER TABLE business_master
ADD COLUMN IF NOT EXISTS owner_user_id BIGINT,
ADD COLUMN IF NOT EXISTS legal_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS address_field_2 VARCHAR(255);

-- Convert coarse integer coordinates to decimal GPS precision.
ALTER TABLE business_master
ALTER COLUMN latitude TYPE DECIMAL(19,7) USING latitude::DECIMAL(19,7),
ALTER COLUMN longitude TYPE DECIMAL(19,7) USING longitude::DECIMAL(19,7);

ALTER TABLE business_master
ADD CONSTRAINT fk_business_master_owner_user
FOREIGN KEY (owner_user_id) REFERENCES users(id);

CREATE INDEX IF NOT EXISTS idx_business_master_status ON business_master(status);
CREATE INDEX IF NOT EXISTS idx_business_master_vertical_id ON business_master(vertical_id);
CREATE INDEX IF NOT EXISTS idx_business_master_owner_user_id ON business_master(owner_user_id);
