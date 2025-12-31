-- 1. Convert BIGINT → BOOLEAN
ALTER TABLE categories
ALTER COLUMN active_status
TYPE BOOLEAN
USING (active_status <> 0);

-- 2. Set default value
ALTER TABLE categories
ALTER COLUMN active_status SET DEFAULT true;