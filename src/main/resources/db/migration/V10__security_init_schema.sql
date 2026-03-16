CREATE TABLE "users" (
    "id" BIGINT PRIMARY KEY,
    "email" VARCHAR(255),
    "username" VARCHAR(255),
    "password" VARCHAR(255),
    "name" VARCHAR(255) NOT NULL,
    "phone" VARCHAR(20),
    "enabled" BOOLEAN NOT NULL DEFAULT TRUE,
    "email_verified" BOOLEAN NOT NULL DEFAULT FALSE,
    "phone_verified" BOOLEAN NOT NULL DEFAULT FALSE,
    "account_locked" BOOLEAN NOT NULL DEFAULT FALSE,
    "created_at" TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    "updated_at" TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX "idx_users_phone_unique"
ON "users" ("phone")
WHERE "phone" IS NOT NULL;

CREATE TABLE "roles" (
    "id" BIGINT PRIMARY KEY,
    "name" VARCHAR(255) NOT NULL
);

CREATE TABLE "users_roles" (
    "user_id" BIGINT NOT NULL,
    "role_id" BIGINT NOT NULL,
    PRIMARY KEY ("user_id", "role_id"),
    CONSTRAINT "fk_users_roles_user"
        FOREIGN KEY ("user_id") REFERENCES "users"("id"),
    CONSTRAINT "fk_users_roles_role"
        FOREIGN KEY ("role_id") REFERENCES "roles"("id")
);

COMMENT ON COLUMN "users_roles"."user_id" IS 'References users(id)';
COMMENT ON COLUMN "users_roles"."role_id" IS 'References roles(id)';

CREATE TABLE "permissions" (
    "id" BIGINT PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE "role_permissions" (
    "role_id" BIGINT NOT NULL,
    "permission_id" BIGINT NOT NULL,
    PRIMARY KEY ("role_id", "permission_id"),
    CONSTRAINT "fk_role_permissions_role"
        FOREIGN KEY ("role_id") REFERENCES "roles"("id"),
    CONSTRAINT "fk_role_permissions_permission"
        FOREIGN KEY ("permission_id") REFERENCES "permissions"("id")
);

CREATE INDEX "idx_role_permissions_role_id" ON "role_permissions"("role_id");
