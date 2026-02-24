-- Add optional category scoping and prompt_text compatibility for dynamic sections.

ALTER TABLE sections
ADD COLUMN IF NOT EXISTS category_id BIGINT,
ADD COLUMN IF NOT EXISTS prompt_text VARCHAR(255);

UPDATE sections
SET prompt_text = prompt
WHERE prompt_text IS NULL;

ALTER TABLE sections
ALTER COLUMN prompt DROP NOT NULL;

ALTER TABLE sections
ADD CONSTRAINT fk_sections_category
FOREIGN KEY (category_id) REFERENCES categories(id);

CREATE INDEX IF NOT EXISTS idx_sections_vertical_category
ON sections(vertical_id, category_id);

-- Validate section category/vertical consistency inside this module query layer.
CREATE OR REPLACE FUNCTION validate_section_category_vertical_match()
RETURNS TRIGGER AS $$
DECLARE
    category_vertical_id BIGINT;
BEGIN
    IF NEW.category_id IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT business_vertical_id
    INTO category_vertical_id
    FROM categories
    WHERE id = NEW.category_id;

    IF category_vertical_id IS NULL THEN
        RAISE EXCEPTION 'Category % does not exist', NEW.category_id;
    END IF;

    IF category_vertical_id <> NEW.vertical_id THEN
        RAISE EXCEPTION 'Section vertical_id % must match category vertical_id %', NEW.vertical_id, category_vertical_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sections_validate_category_vertical ON sections;

CREATE TRIGGER trg_sections_validate_category_vertical
BEFORE INSERT OR UPDATE ON sections
FOR EACH ROW
EXECUTE FUNCTION validate_section_category_vertical_match();
