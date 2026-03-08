-- Add relational integrity and deduplication rules for business-category and business-child tables.

ALTER TABLE business_category_mapping
ADD CONSTRAINT uq_business_category_mapping UNIQUE (business_id, category_id);

ALTER TABLE categories
ADD CONSTRAINT fk_categories_vertical
FOREIGN KEY (business_vertical_id) REFERENCES business_verticals(id) NOT VALID;

ALTER TABLE business_master
ADD CONSTRAINT fk_business_master_vertical
FOREIGN KEY (vertical_id) REFERENCES business_verticals(id) NOT VALID,
ADD CONSTRAINT fk_business_master_municipality
FOREIGN KEY (municipality_id) REFERENCES municipality_master(id) NOT VALID;

ALTER TABLE business_category_mapping
ADD CONSTRAINT fk_business_category_business
FOREIGN KEY (business_id) REFERENCES business_master(id) NOT VALID,
ADD CONSTRAINT fk_business_category_category
FOREIGN KEY (category_id) REFERENCES categories(id) NOT VALID;

ALTER TABLE section_fields
ADD CONSTRAINT fk_section_fields_section
FOREIGN KEY (section_id) REFERENCES sections(id) NOT VALID;

ALTER TABLE business_data
ADD CONSTRAINT fk_business_data_business
FOREIGN KEY (business_id) REFERENCES business_master(id) NOT VALID,
ADD CONSTRAINT fk_business_data_section
FOREIGN KEY (section_id) REFERENCES sections(id) NOT VALID,
ADD CONSTRAINT fk_business_data_section_group
FOREIGN KEY (section_group_id) REFERENCES section_groups(id) NOT VALID;

ALTER TABLE section_groups
ADD CONSTRAINT fk_section_groups_business
FOREIGN KEY (business_id) REFERENCES business_master(id) NOT VALID,
ADD CONSTRAINT fk_section_groups_section
FOREIGN KEY (section_id) REFERENCES sections(id) NOT VALID;

ALTER TABLE document_definitions
ADD CONSTRAINT fk_document_definitions_vertical
FOREIGN KEY (vertical_id) REFERENCES business_verticals(id) NOT VALID;

ALTER TABLE business_documents
ADD CONSTRAINT fk_business_documents_business
FOREIGN KEY (business_id) REFERENCES business_master(id) NOT VALID,
ADD CONSTRAINT fk_business_documents_definition
FOREIGN KEY (document_definition_id) REFERENCES document_definitions(id) NOT VALID;

ALTER TABLE business_kyc
ADD CONSTRAINT fk_business_kyc_business
FOREIGN KEY (business_id) REFERENCES business_master(id) NOT VALID;

ALTER TABLE business_kyc_history
ADD CONSTRAINT fk_business_kyc_history_business
FOREIGN KEY (business_id) REFERENCES business_master(id) NOT VALID;

ALTER TABLE attribute_definitions
ADD CONSTRAINT fk_attribute_definitions_vertical
FOREIGN KEY (vertical_id) REFERENCES business_verticals(id) NOT VALID;

ALTER TABLE business_attributes
ADD CONSTRAINT fk_business_attributes_business
FOREIGN KEY (business_id) REFERENCES business_master(id) NOT VALID,
ADD CONSTRAINT fk_business_attributes_definition
FOREIGN KEY (attribute_id) REFERENCES attribute_definitions(id) NOT VALID;
