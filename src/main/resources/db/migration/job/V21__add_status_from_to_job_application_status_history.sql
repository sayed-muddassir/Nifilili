-- V21: Replace single 'status' column with 'status_from' + 'status_to' in job_application_status_history.
-- Requirement: audit trail must capture both the previous and new status for each transition.
-- status_from is nullable because the first entry (initial application) has no previous status.

ALTER TABLE "job_application_status_history"
    RENAME COLUMN "status" TO "status_to";

ALTER TABLE "job_application_status_history"
    ADD COLUMN "status_from" VARCHAR(255) NULL;

COMMENT ON COLUMN "job_application_status_history"."status_from" IS 'Previous application status before transition (NULL for initial application)';
COMMENT ON COLUMN "job_application_status_history"."status_to" IS 'New application status after transition';
