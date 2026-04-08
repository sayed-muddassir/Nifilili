ALTER TABLE "business_master" ADD COLUMN "profile_image_url" VARCHAR(255) NULL;
ALTER TABLE "business_master" ADD COLUMN "banner_image_url" VARCHAR(255) NULL;

CREATE TABLE "business_images" (
    "id" BIGINT PRIMARY KEY,
    "business_id" BIGINT NOT NULL,
    "image_url" VARCHAR(255) NOT NULL,
    "added_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT "fk_business_images_business"
        FOREIGN KEY ("business_id") REFERENCES "business_master" ("id")
);
