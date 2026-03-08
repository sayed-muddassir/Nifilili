CREATE TABLE "job_categories"(
    "id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "created_by" BIGINT NOT NULL,
    "updated_by" BIGINT NOT NULL
);
ALTER TABLE
    "job_categories" ADD PRIMARY KEY("id");
CREATE TABLE "job_openings"(
    "id" BIGINT NOT NULL,
    "business_id" BIGINT NOT NULL,
    "job_category_id" BIGINT NOT NULL,
    "title" VARCHAR(255) NOT NULL,
    "description" TEXT NOT NULL,
    "job_type" VARCHAR(255) NOT NULL,
    "municipality_id" BIGINT NOT NULL,
    "ward_number" BIGINT NOT NULL,
    "tole_name" VARCHAR(255) NOT NULL,
    "postal_code" VARCHAR(255) NOT NULL,
    "is_remote" BOOLEAN NOT NULL,
    "salary_range_min" DOUBLE PRECISION NOT NULL,
    "salary_range_max" DOUBLE PRECISION NOT NULL,
    "number_of_openings" INTEGER NOT NULL,
    "application_deadline" DATE NOT NULL,
    "skills" jsonb NOT NULL,
    "view_count" BIGINT NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "created_by" BIGINT NOT NULL,
    "updated_by" BIGINT NOT NULL
);
ALTER TABLE
    "job_openings" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "job_openings"."job_type" IS 'full time, part time, freelance, internship, contract';
COMMENT
ON COLUMN
    "job_openings"."status" IS 'draft, open, closed';
CREATE TABLE "job_questions"(
    "id" BIGINT NOT NULL,
    "job_opening_id" BIGINT NOT NULL,
    "question_text" TEXT NOT NULL,
    "required" BOOLEAN NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "job_questions" ADD PRIMARY KEY("id");
CREATE TABLE "job_applications"(
    "id" BIGINT NOT NULL,
    "job_opening_id" BIGINT NOT NULL,
    "user_id" BIGINT NOT NULL,
    resume_url VARCHAR(1024) NOT NULL,
    "cover_letter" TEXT NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "withdrawal_reason" TEXT NOT NULL,
    "withdrawn_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "job_applications" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "job_applications"."status" IS 'RECEIVED, REVIEWED, INTERVIEWING, HIRED, REJECTED, SHORTLISTED';
CREATE TABLE "job_application_answers"(
    "id" BIGINT NOT NULL,
    "job_application_id" BIGINT NOT NULL,
    "job_question_id" BIGINT NOT NULL,
    "answer" TEXT NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "job_application_answers" ADD PRIMARY KEY("id");
CREATE TABLE "job_application_status_history"(
    "id" BIGINT NOT NULL,
    "job_application_id" BIGINT NOT NULL,
    "status" VARCHAR(255) NOT NULL,
    "notes" TEXT NOT NULL,
    "changed_by" BIGINT NOT NULL,
    "changed_date" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "job_application_status_history" ADD PRIMARY KEY("id");