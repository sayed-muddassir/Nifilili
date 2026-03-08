-- Convert section_groups.name from BIGINT to VARCHAR

ALTER TABLE section_groups
ALTER COLUMN name
TYPE VARCHAR(255)
USING name::VARCHAR;

-- (Optional) keep NOT NULL constraint explicit
ALTER TABLE section_groups
ALTER COLUMN name SET NOT NULL;
