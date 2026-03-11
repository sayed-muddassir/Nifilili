-- ============================================================
-- V13: Token Management, Session Tracking, and Account Lockout
-- ============================================================

-- Add email_verified and account_locked flags to users
ALTER TABLE "users" ADD COLUMN "email_verified" BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE "users" ADD COLUMN "account_locked" BOOLEAN NOT NULL DEFAULT FALSE;

-- Mark existing seeded users as verified
UPDATE "users" SET "email_verified" = TRUE WHERE "id" IN (1, 2);

-- Refresh tokens with device tracking
CREATE TABLE "refresh_tokens" (
    "id"            BIGINT PRIMARY KEY,
    "user_id"       BIGINT NOT NULL REFERENCES "users"("id") ON DELETE CASCADE,
    "token"         VARCHAR(512) NOT NULL UNIQUE,
    "device_name"   VARCHAR(255),
    "ip_address"    VARCHAR(45),
    "user_agent"    VARCHAR(512),
    "expires_at"    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "revoked"       BOOLEAN NOT NULL DEFAULT FALSE,
    "created_at"    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX "idx_refresh_tokens_user_id" ON "refresh_tokens"("user_id");
CREATE INDEX "idx_refresh_tokens_token" ON "refresh_tokens"("token");
CREATE INDEX "idx_refresh_tokens_expires_at" ON "refresh_tokens"("expires_at");

-- Login attempts for lockout tracking
CREATE TABLE "login_attempts" (
    "id"            BIGINT PRIMARY KEY,
    "user_id"       BIGINT REFERENCES "users"("id") ON DELETE CASCADE,
    "username"      VARCHAR(255) NOT NULL,
    "ip_address"    VARCHAR(45),
    "success"       BOOLEAN NOT NULL,
    "attempted_at"  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX "idx_login_attempts_user_id" ON "login_attempts"("user_id");
CREATE INDEX "idx_login_attempts_username_attempted" ON "login_attempts"("username", "attempted_at");

-- Login history (audit trail for successful logins)
CREATE TABLE "login_history" (
    "id"            BIGINT PRIMARY KEY,
    "user_id"       BIGINT NOT NULL REFERENCES "users"("id") ON DELETE CASCADE,
    "ip_address"    VARCHAR(45),
    "user_agent"    VARCHAR(512),
    "device_name"   VARCHAR(255),
    "logged_in_at"  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX "idx_login_history_user_id" ON "login_history"("user_id");
