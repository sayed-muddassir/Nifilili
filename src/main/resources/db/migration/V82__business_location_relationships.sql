ALTER TABLE "district_master"
    ADD CONSTRAINT "fk_district_master_province"
        FOREIGN KEY ("province_id") REFERENCES "province_master" ("id");

ALTER TABLE "municipality_master"
    ADD CONSTRAINT "fk_municipality_master_district"
        FOREIGN KEY ("district_id") REFERENCES "district_master" ("id");

CREATE INDEX "idx_district_master_province_id" ON "district_master" ("province_id");
CREATE INDEX "idx_municipality_master_district_id" ON "municipality_master" ("district_id");
