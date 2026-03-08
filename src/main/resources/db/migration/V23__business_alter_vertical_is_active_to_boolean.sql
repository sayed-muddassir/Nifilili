-- 1. Convert BIGINT → BOOLEAN
ALTER TABLE business_verticals
ALTER COLUMN is_active
TYPE BOOLEAN
USING (is_active <> 0);

-- 2. Set default value
ALTER TABLE business_verticals
ALTER COLUMN is_active SET DEFAULT true;