-- Support explicit claim ownership for admin-seeded businesses.

ALTER TABLE business_master
ADD COLUMN IF NOT EXISTS claimed_by_user_id BIGINT;

ALTER TABLE business_master
ADD CONSTRAINT fk_business_master_claimed_by_user
FOREIGN KEY (claimed_by_user_id) REFERENCES users(id);

CREATE INDEX IF NOT EXISTS idx_business_master_source_claim_status
ON business_master(source, is_claimed, status);
