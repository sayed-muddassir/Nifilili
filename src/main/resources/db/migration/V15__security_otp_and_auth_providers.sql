CREATE TABLE "otp_tokens" (
    "id" BIGINT PRIMARY KEY,
    "identifier" VARCHAR(100) NOT NULL,
    "identifier_type" VARCHAR(10) NOT NULL,
    "otp" VARCHAR(6) NOT NULL,
    "purpose" VARCHAR(20) NOT NULL,
    "expires_at" TIMESTAMP NOT NULL,
    "used" BOOLEAN NOT NULL DEFAULT FALSE,
    "attempts" INT NOT NULL DEFAULT 0,
    "created_at" TIMESTAMP NOT NULL,
    CONSTRAINT "chk_otp_identifier_type"
        CHECK ("identifier_type" IN ('PHONE', 'EMAIL')),
    CONSTRAINT "chk_otp_purpose"
        CHECK ("purpose" IN ('LOGIN', 'SIGNUP', 'PASSWORD_RESET', 'VERIFY_PHONE', 'VERIFY_EMAIL'))
);

CREATE INDEX "idx_otp_tokens_identifier_purpose" ON "otp_tokens"("identifier", "purpose", "used");
CREATE INDEX "idx_otp_tokens_lookup" ON "otp_tokens"("identifier", "otp", "used");

CREATE TABLE "user_auth_providers" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL REFERENCES "users"("id") ON DELETE CASCADE,
    "provider_type" VARCHAR(20) NOT NULL,
    "provider_user_id" VARCHAR(255),
    "linked_at" TIMESTAMP NOT NULL,
    CONSTRAINT "chk_provider_type"
        CHECK ("provider_type" IN ('LOCAL_EMAIL', 'LOCAL_PHONE', 'GOOGLE', 'FACEBOOK')),
    CONSTRAINT "uq_user_provider"
        UNIQUE ("user_id", "provider_type")
);

CREATE INDEX "idx_user_auth_providers_user" ON "user_auth_providers"("user_id");
