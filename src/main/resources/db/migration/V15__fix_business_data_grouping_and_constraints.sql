-- Allow non-repeatable section rows without synthetic group IDs and add query-friendly indexes.

ALTER TABLE business_data
ALTER COLUMN section_group_id DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_business_data_business_section
ON business_data(business_id, section_id);

CREATE INDEX IF NOT EXISTS idx_business_data_business_section_group
ON business_data(business_id, section_id, section_group_id);
