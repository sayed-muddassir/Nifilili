CREATE TABLE "email_verification_tokens" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL REFERENCES "users"("id") ON DELETE CASCADE,
    "token" VARCHAR(512) NOT NULL UNIQUE,
    "expires_at" TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "used" BOOLEAN NOT NULL DEFAULT FALSE,
    "created_at" TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX "idx_email_verification_tokens_user_id" ON "email_verification_tokens"("user_id");
CREATE INDEX "idx_email_verification_tokens_token" ON "email_verification_tokens"("token");

CREATE TABLE "password_reset_tokens" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL REFERENCES "users"("id") ON DELETE CASCADE,
    "token" VARCHAR(512) NOT NULL UNIQUE,
    "otp" VARCHAR(6),
    "type" VARCHAR(10) NOT NULL CHECK ("type" IN ('LINK', 'OTP')),
    "expires_at" TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "used" BOOLEAN NOT NULL DEFAULT FALSE,
    "created_at" TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX "idx_password_reset_tokens_user_id" ON "password_reset_tokens"("user_id");
CREATE INDEX "idx_password_reset_tokens_token" ON "password_reset_tokens"("token");
