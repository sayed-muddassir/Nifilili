-- V22__search_init_schema.sql
-- Initializes full-text search capability for business_master table
-- This migration enables keyword-based search with relevance ranking and geo-filtering

-- ──────────────────────────────────────────────
-- 1. Enable Required PostgreSQL Extensions
-- ──────────────────────────────────────────────

-- pg_trgm: Enables trigram similarity searches (fuzzy matching on names)
-- Used for fallback name matching when full-text search doesn't match exactly
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ──────────────────────────────────────────────
-- 2. Add Full-Text Search Vector Column
-- ──────────────────────────────────────────────

-- This column automatically combines name, legal_name, and business_summary into
-- a searchable vector. PostgreSQL maintains it automatically whenever these fields change.
-- Weights control relevance: A (name) > B (legal_name) > C (summary)
ALTER TABLE business_master
ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('english', COALESCE(name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(legal_name, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(business_summary, '')), 'C')
    ) STORED;

COMMENT ON COLUMN business_master.search_vector IS
    'Full-text search vector combining business name (weight A), legal name (weight B), and business summary (weight C). Automatically maintained by PostgreSQL on INSERT/UPDATE.';

-- ──────────────────────────────────────────────
-- 3. Create GIN Index for Full-Text Search
-- ──────────────────────────────────────────────

-- GIN index is optimal for FTS queries. It enables fast matching using the @@ operator
-- and scoring using ts_rank(). Trades slower writes for much faster reads.
CREATE INDEX idx_business_search_vector_gin ON business_master
    USING GIN (search_vector);

-- ──────────────────────────────────────────────
-- 4. Create Trigram Index for Fuzzy Name Matching
-- ──────────────────────────────────────────────

-- Trigram index enables the `%` operator for fuzzy matching on business names.
-- Used as a fallback when exact FTS doesn't match (e.g., typos, abbreviations).
CREATE INDEX idx_business_name_trigram ON business_master
    USING GIN (name gin_trgm_ops);

-- ──────────────────────────────────────────────
-- 5. Create Indexes for Geo-Distance Filtering
-- ──────────────────────────────────────────────

-- B-tree index on latitude for fast bounding box filtering
CREATE INDEX idx_business_latitude ON business_master (latitude);

-- Composite index: latitude + longitude for efficient multi-column range queries
CREATE INDEX idx_business_latitude_longitude ON business_master (latitude, longitude);

-- ──────────────────────────────────────────────
-- 6. Optional: Partial Index for Published Status
-- ──────────────────────────────────────────────

-- This index is smaller and faster since it only indexes the rows we actually search.
-- The BusinessSearchQueryBuilder always filters by status='PUBLISHED', so this helps.
CREATE INDEX idx_business_status_published ON business_master (status)
    WHERE status = 'PUBLISHED';

