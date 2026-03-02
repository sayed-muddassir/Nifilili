-- Strengthen KYC review workflow to support correction banners and per-document review metadata.

ALTER TABLE business_documents
ALTER COLUMN rejection_reason DROP NOT NULL;

ALTER TABLE business_documents
ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP(0) WITHOUT TIME ZONE,
ADD COLUMN IF NOT EXISTS reviewed_by_user_id BIGINT;

ALTER TABLE business_documents
ADD CONSTRAINT fk_business_documents_reviewed_by
FOREIGN KEY (reviewed_by_user_id) REFERENCES users(id);

ALTER TABLE business_kyc
ADD COLUMN IF NOT EXISTS rejection_reason TEXT;

CREATE INDEX IF NOT EXISTS idx_business_kyc_status
ON business_kyc(kyc_status);

CREATE INDEX IF NOT EXISTS idx_business_documents_business_status
ON business_documents(business_id, status);
