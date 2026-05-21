# Database Changes Required for Keyword-Based Search

## Analysis Summary

Your search module is **well-architected** but relies on PostgreSQL database features that **don't yet exist in your schema**. Here's what needs to be added:

---

## What the Code Expects

The `BusinessSearchQueryBuilder` uses:

1. **Full-Text Search (FTS)**
   - Column: `business_master.search_vector` (tsvector type)
   - Operator: `@@` (FTS matching)
   - Function: `ts_rank()` for relevance ranking
   - Function: `plainto_tsquery()` to convert keywords to FTS queries

2. **Trigram Similarity**
   - Function: `%` operator for fuzzy matching on `business_master.name`
   - Requires: `pg_trgm` PostgreSQL extension

3. **Geo Filtering**
   - Columns: `business_master.latitude`, `business_master.longitude` (already exist)
   - Needs: B-tree indexes for efficient bounding box queries

---

## Required Database Changes

### 1. Enable PostgreSQL Extensions

```sql
-- Required for trigram similarity search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Optional: enables additional index types (useful for FTS optimization)
CREATE EXTENSION IF NOT EXISTS btree_gin;
```

### 2. Add search_vector Column to business_master

```sql
ALTER TABLE business_master ADD COLUMN search_vector tsvector 
    GENERATED ALWAYS AS (
        setweight(to_tsvector('english', COALESCE(name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(legal_name, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(business_summary, '')), 'C')
    ) STORED;
```

**Explanation:**
- `GENERATED ALWAYS AS ... STORED`: PostgreSQL automatically maintains this column whenever name/legal_name/business_summary change
- `to_tsvector('english', ...)`: Converts text to searchable tokens using English stemming (stopword removal, etc.)
- `setweight()`: Assigns relevance weights
  - `'A'`: Highest weight → name matches rank highest
  - `'B'`: Medium weight → legal_name
  - `'C'`: Lower weight → business_summary
- The `||` operator concatenates multiple tsvectors

### 3. Create GIN Index on search_vector (Critical for Performance)

```sql
-- GIN index enables fast full-text search lookups
CREATE INDEX idx_business_search_vector_gin ON business_master 
    USING GIN (search_vector);
```

**Why GIN?**
- ~5-10x faster than GIST for FTS
- Slower to build but queries are much faster
- Perfect for read-heavy search workloads

### 4. Create Indexes for Trigram Search

```sql
-- Trigram index enables fast `%` operator searches on business name
CREATE INDEX idx_business_name_trigram ON business_master 
    USING GIN (name gin_trgm_ops);
```

### 5. Create Indexes for Geo Filtering

```sql
-- B-tree indexes enable efficient bounding box filters
CREATE INDEX idx_business_latitude ON business_master (latitude);
CREATE INDEX idx_business_latitude_longitude ON business_master (latitude, longitude);
```

### 6. (Optional) Create Composite Index for Common Query Patterns

```sql
-- Combines search filtering with status check (used in all searches)
CREATE INDEX idx_business_status_search_vector ON business_master (status) 
    INCLUDE (search_vector);
```

---

## Create a Flyway Migration

Create a new migration file: `V22__search_init_schema.sql`

```sql
-- V22__search_init_schema.sql
-- Adds full-text search capability to business_master table

-- 1. Enable required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 2. Add search_vector column (auto-maintained by PostgreSQL)
ALTER TABLE business_master ADD COLUMN search_vector tsvector 
    GENERATED ALWAYS AS (
        setweight(to_tsvector('english', COALESCE(name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(legal_name, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(business_summary, '')), 'C')
    ) STORED;

-- 3. GIN index for full-text search (fast reads)
CREATE INDEX idx_business_search_vector_gin ON business_master 
    USING GIN (search_vector);

-- 4. Trigram index for fuzzy name matching
CREATE INDEX idx_business_name_trigram ON business_master 
    USING GIN (name gin_trgm_ops);

-- 5. Geo-filtering indexes (bounding box queries)
CREATE INDEX idx_business_latitude ON business_master (latitude);
CREATE INDEX idx_business_latitude_longitude ON business_master (latitude, longitude);

-- 6. Composite index for common search + status filter pattern
CREATE INDEX idx_business_status_published ON business_master (status) 
    WHERE status = 'PUBLISHED';

-- Add comment explaining the search_vector column
COMMENT ON COLUMN business_master.search_vector IS 
    'Full-text search vector combining name (weight A), legal_name (weight B), and business_summary (weight C). Automatically maintained by PostgreSQL.';
```

---

## How the Search Works (Post-Migration)

### Step 1: Build Bounding Box Filter
User provides: `latitude=27.7172, longitude=85.3240, distance_km=5`
```java
// GeoCalculationUtils.computeBoundingBox() in Java calculates:
latMin = 27.716, latMax = 27.718
lngMin = 85.321, lngMax = 85.327
```

### Step 2: Execute SQL Query
```sql
SELECT b.id, b.name, /* ... other columns ... */,
       ts_rank(b.search_vector, plainto_tsquery('english', :keyword)) AS search_rank,
       (6371 * acos(...)) AS distance_km
FROM business_master b
WHERE b.status = 'PUBLISHED'
  AND b.search_vector @@ plainto_tsquery('english', :keyword)
  AND b.name % :keyword  -- OR trigram similarity fallback
  AND b.latitude BETWEEN :latMin AND :latMax
  AND b.longitude BETWEEN :lngMin AND :lngMax
ORDER BY search_rank DESC
LIMIT :limit OFFSET :offset;
```

### Step 3: Query Uses These Indexes
- `idx_business_search_vector_gin` → Fast FTS matching via `@@` operator
- `idx_business_name_trigram` → Fast fuzzy name matching via `%` operator
- `idx_business_latitude` & `idx_business_latitude_longitude` → Fast bounding box filtering

---

## Timeline: Execute These Changes

### Phase 1: Only Keyword Search (Immediate)
Run migration with just:
- `pg_trgm` extension
- `search_vector` column
- `idx_business_search_vector_gin` index

Then your code will work for keyword-only search.

### Phase 2: Add Geo Search (Next)
Add:
- `idx_business_latitude`
- `idx_business_latitude_longitude`

Then geo-filtering will work.

### Phase 3: Add Other Filters (Later)
All other filters (verified_only, minRating, etc.) work with existing columns—no new schema changes needed.

---

## Testing the Setup

After running the migration:

### 1. Verify extension installed
```sql
SELECT extname FROM pg_extension WHERE extname = 'pg_trgm';
-- Should return: pg_trgm
```

### 2. Verify search_vector column exists
```sql
SELECT column_name, data_type FROM information_schema.columns 
WHERE table_name = 'business_master' AND column_name = 'search_vector';
-- Should return: search_vector | tsvector
```

### 3. Test FTS query manually
```sql
SELECT id, name, ts_rank(search_vector, query) AS rank
FROM business_master, plainto_tsquery('english', 'plumber') AS query
WHERE search_vector @@ query
ORDER BY rank DESC
LIMIT 10;
```

### 4. Test trigram query manually
```sql
SELECT id, name, similarity(name, 'electician') AS sim
FROM business_master
WHERE name % 'electician'
ORDER BY sim DESC
LIMIT 10;
```

---

## Database Size Impact

- **search_vector column**: ~500 bytes per row (smaller than most text fields)
- **GIN index**: ~2-3x the column size (depends on unique tokens)
- For 100k businesses: ~50-60 MB total

---

## Next Steps

1. ✅ File: `/Users/sayedmuddassirhussain/Library/CloudStorage/OneDrive-Nagarro/Desktop/Personal Projects/Nifilili/src/main/resources/db/migration/V22__search_init_schema.sql` (I'll create this)
2. Run: `mvn clean compile`
3. Run: `mvn spring-boot:run` (Flyway auto-applies the migration)
4. Test keyword search via Postman/API

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Migration fails: "GENERATED ALWAYS" not supported | PostgreSQL < 12. Upgrade or use trigger-based approach |
| Very slow FTS queries | Run `ANALYZE business_master;` to update statistics |
| Index not used | Check with `EXPLAIN ANALYZE` → may need to increase `random_page_cost` |
| Memory issues during index creation | Run during off-hours or create index CONCURRENTLY |


