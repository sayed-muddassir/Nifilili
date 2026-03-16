CREATE TABLE "user_profiles" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL UNIQUE REFERENCES "users"("id") ON DELETE CASCADE,
    "avatar_url" VARCHAR(1024),
    "bio" TEXT,
    "date_of_birth" DATE,
    "gender" VARCHAR(20),
    "street_address" VARCHAR(500),
    "city" VARCHAR(100),
    "state" VARCHAR(100),
    "country" VARCHAR(100),
    "postal_code" VARCHAR(20),
    "created_at" TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    "updated_at" TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX "idx_user_profiles_user_id" ON "user_profiles"("user_id");
