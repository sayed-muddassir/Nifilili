-- V20: Harden users table and seed missing application roles.
-- Adds: phone (nullable), enabled (NOT NULL, default TRUE), created_at, updated_at.
-- Seeds: ROLE_BUSINESS_OWNER, ROLE_FREELANCER, ROLE_SUPPORT, ROLE_TECH.

ALTER TABLE "users"
    ADD COLUMN IF NOT EXISTS "phone"      VARCHAR(20),
    ADD COLUMN IF NOT EXISTS "enabled"    BOOLEAN     NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS "created_at" TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS "updated_at" TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW();

INSERT INTO "roles" ("id", "name") VALUES
    (3, 'ROLE_BUSINESS_OWNER'),
    (4, 'ROLE_FREELANCER'),
    (5, 'ROLE_SUPPORT'),
    (6, 'ROLE_TECH')
ON CONFLICT ("id") DO NOTHING;
