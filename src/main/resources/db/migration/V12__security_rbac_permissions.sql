-- ============================================================
-- V12: Role-Based Access Control with Granular Permissions
-- ============================================================

-- Permissions table
CREATE TABLE "permissions" (
    "id"   BIGINT PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL UNIQUE
);

-- Role-Permission junction table
CREATE TABLE "role_permissions" (
    "role_id"       BIGINT NOT NULL REFERENCES "roles"("id"),
    "permission_id" BIGINT NOT NULL REFERENCES "permissions"("id"),
    PRIMARY KEY ("role_id", "permission_id")
);

CREATE INDEX "idx_role_permissions_role_id" ON "role_permissions"("role_id");

-- Seed all permissions
INSERT INTO "permissions" ("id", "name") VALUES
    (1,  'BUSINESS_CREATE'),
    (2,  'BUSINESS_MANAGE_OWN'),
    (3,  'BUSINESS_MANAGE_ALL'),
    (4,  'BUSINESS_VIEW_ALL'),
    (5,  'KYC_SUBMIT'),
    (6,  'KYC_REVIEW'),
    (7,  'JOB_CREATE'),
    (8,  'JOB_MANAGE_OWN'),
    (9,  'JOB_APPLY'),
    (10, 'JOB_MANAGE_ALL'),
    (11, 'OFFERING_CREATE'),
    (12, 'OFFERING_MANAGE_OWN'),
    (13, 'OFFERING_MANAGE_ALL'),
    (14, 'ORDER_PLACE'),
    (15, 'ORDER_MANAGE_OWN'),
    (16, 'ORDER_MANAGE_BUSINESS'),
    (17, 'ORDER_MANAGE_ALL'),
    (18, 'QUOTE_REQUEST'),
    (19, 'QUOTE_RESPOND'),
    (20, 'QUOTE_MANAGE_ALL'),
    (21, 'ACCOUNT_MANAGE_OWN'),
    (22, 'ACCOUNT_MANAGE_ALL'),
    (23, 'SYSTEM_ADMIN'),
    (24, 'SUPPORT_MANAGE');

-- ROLE_USER (id=2): end-user capabilities
INSERT INTO "role_permissions" ("role_id", "permission_id") VALUES
    (2, 1),   -- BUSINESS_CREATE
    (2, 5),   -- KYC_SUBMIT
    (2, 9),   -- JOB_APPLY
    (2, 11),  -- OFFERING_CREATE
    (2, 14),  -- ORDER_PLACE
    (2, 15),  -- ORDER_MANAGE_OWN
    (2, 18),  -- QUOTE_REQUEST
    (2, 21);  -- ACCOUNT_MANAGE_OWN

-- ROLE_BUSINESS_OWNER (id=3): inherits USER + business management
INSERT INTO "role_permissions" ("role_id", "permission_id") VALUES
    (3, 1),   -- BUSINESS_CREATE
    (3, 2),   -- BUSINESS_MANAGE_OWN
    (3, 5),   -- KYC_SUBMIT
    (3, 7),   -- JOB_CREATE
    (3, 8),   -- JOB_MANAGE_OWN
    (3, 9),   -- JOB_APPLY
    (3, 11),  -- OFFERING_CREATE
    (3, 12),  -- OFFERING_MANAGE_OWN
    (3, 14),  -- ORDER_PLACE
    (3, 15),  -- ORDER_MANAGE_OWN
    (3, 16),  -- ORDER_MANAGE_BUSINESS
    (3, 18),  -- QUOTE_REQUEST
    (3, 19),  -- QUOTE_RESPOND
    (3, 21);  -- ACCOUNT_MANAGE_OWN

-- ROLE_FREELANCER (id=4): inherits USER + freelancer offering/quote
INSERT INTO "role_permissions" ("role_id", "permission_id") VALUES
    (4, 1),   -- BUSINESS_CREATE
    (4, 5),   -- KYC_SUBMIT
    (4, 9),   -- JOB_APPLY
    (4, 11),  -- OFFERING_CREATE
    (4, 12),  -- OFFERING_MANAGE_OWN
    (4, 14),  -- ORDER_PLACE
    (4, 15),  -- ORDER_MANAGE_OWN
    (4, 18),  -- QUOTE_REQUEST
    (4, 19),  -- QUOTE_RESPOND
    (4, 21);  -- ACCOUNT_MANAGE_OWN

-- ROLE_ADMIN (id=1): all permissions
INSERT INTO "role_permissions" ("role_id", "permission_id")
    SELECT 1, "id" FROM "permissions";

-- ROLE_SUPPORT (id=5): support-related
INSERT INTO "role_permissions" ("role_id", "permission_id") VALUES
    (5, 22),  -- ACCOUNT_MANAGE_ALL
    (5, 24);  -- SUPPORT_MANAGE

-- ROLE_TECH (id=6): system administration
INSERT INTO "role_permissions" ("role_id", "permission_id") VALUES
    (6, 3),   -- BUSINESS_MANAGE_ALL
    (6, 4),   -- BUSINESS_VIEW_ALL
    (6, 6),   -- KYC_REVIEW
    (6, 10),  -- JOB_MANAGE_ALL
    (6, 13),  -- OFFERING_MANAGE_ALL
    (6, 17),  -- ORDER_MANAGE_ALL
    (6, 20),  -- QUOTE_MANAGE_ALL
    (6, 23);  -- SYSTEM_ADMIN
