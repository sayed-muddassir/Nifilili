-- 1. Convert BIGINT → BOOLEAN
ALTER TABLE business_master
ALTER COLUMN is_claimed
TYPE BOOLEAN
USING (is_claimed <> 0);

-- 2. Set default value
ALTER TABLE business_master
ALTER COLUMN is_claimed SET DEFAULT false;

-- 3. (Optional but recommended) Enforce NOT NULL
ALTER TABLE business_master
ALTER COLUMN is_claimed SET NOT NULL;