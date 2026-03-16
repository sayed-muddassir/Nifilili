INSERT INTO "users" (
    "id", "email", "username", "password", "name", "phone",
    "enabled", "email_verified", "phone_verified", "account_locked",
    "created_at", "updated_at"
) VALUES
    (1, 'ramesh@gmail.com', 'ramesh', '$2a$10$5PiyN0MsG0y886d8xWXtwuLXK0Y7zZwcN5xm82b4oDSVr7yF0O6em', 'ramesh', NULL, TRUE, TRUE, FALSE, FALSE, NOW(), NOW()),
    (2, 'admin@gmail.com', 'admin', '$2a$10$gqHrslMttQWSsDSVRTK1OehkkBiXsJ/a4z2OURU./dizwOQu5Lovu', 'admin', NULL, TRUE, TRUE, FALSE, FALSE, NOW(), NOW());

INSERT INTO "roles" ("id", "name") VALUES
    (1, 'ROLE_ADMIN'),
    (2, 'ROLE_USER'),
    (3, 'ROLE_BUSINESS_OWNER'),
    (4, 'ROLE_FREELANCER'),
    (5, 'ROLE_SUPPORT'),
    (6, 'ROLE_TECH');

INSERT INTO "users_roles" ("user_id", "role_id") VALUES
    (2, 1),
    (1, 2);

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

INSERT INTO "role_permissions" ("role_id", "permission_id") VALUES
    (2, 1),
    (2, 5),
    (2, 9),
    (2, 11),
    (2, 14),
    (2, 15),
    (2, 18),
    (2, 21),
    (3, 1),
    (3, 2),
    (3, 5),
    (3, 7),
    (3, 8),
    (3, 9),
    (3, 11),
    (3, 12),
    (3, 14),
    (3, 15),
    (3, 16),
    (3, 18),
    (3, 19),
    (3, 21),
    (4, 1),
    (4, 5),
    (4, 9),
    (4, 11),
    (4, 12),
    (4, 14),
    (4, 15),
    (4, 18),
    (4, 19),
    (4, 21),
    (5, 22),
    (5, 24),
    (6, 3),
    (6, 4),
    (6, 6),
    (6, 10),
    (6, 13),
    (6, 17),
    (6, 20),
    (6, 23);

INSERT INTO "role_permissions" ("role_id", "permission_id")
SELECT 1, "id" FROM "permissions";
