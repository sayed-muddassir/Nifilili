# Search Implementation Checklist

## What You Have (Code-Side) ✅

| Component | Status | Purpose |
|-----------|--------|---------|
| `BusinessSearchQueryBuilder` | ✅ Ready | Builds native SQL queries dynamically |
| `BusinessSearchRepository` | ✅ Ready | Executes queries via EntityManager |
| `BusinessSearchService` | ✅ Ready | Orchestrates the search pipeline |
| `BusinessSearchResultMapper` | ✅ Ready | Maps raw SQL results to DTOs |
| `SearchFilterContext` | ✅ Ready | Holds filter values for building queries |
| `GeoCalculationUtils` | ✅ Ready | Computes bounding boxes for geo search |
| FTS-aware column indexes | ✅ Ready | Query builder expects search_vector column |

## What You Need (Database-Side) 🔴

| Change | Migration | Effect | Enables |
|--------|-----------|--------|---------|
| `pg_trgm` extension | **V22** | Enables `%` operator | Fuzzy name matching |
| `search_vector` column | **V22** | Auto-indexed full-text combos of name, legal_name, summary | Keyword search ranking |
| `idx_business_search_vector_gin` | **V22** | Speeds up FTS @@ operator | Fast keyword matching |
| `idx_business_name_trigram` | **V22** | Speeds up % operator | Fast fuzzy name search |
| `idx_business_latitude` | **V22** | B-tree on latitude | Geo bounding box filtering |
| `idx_business_latitude_longitude` | **V22** | Composite B-tree | Fast multi-dimension geo filters |

## Quick Start (5 Minutes)

### Step 1: Add Migration File
✅ Already created at:
```
src/main/resources/db/migration/V22__search_init_schema.sql
```

### Step 2: Compile
```bash
mvn clean compile
```

### Step 3: Run Application
```bash
mvn spring-boot:run
```
Flyway automatically applies the migration on startup.

### Step 4: Verify in Database
```sql
-- Connect to your PostgreSQL database and run:
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'business_master' AND column_name = 'search_vector';

-- Should output: search_vector | tsvector
```

### Step 5: Test Search API

#### Via curl (keyword only):
```bash
curl -X POST http://localhost:8080/api/v1/search/business \
  -H "Content-Type: application/json" \
  -d '{
    "criteria": {
      "query": "plumber"
    },
    "page": 0,
    "size": 20
  }'
```

#### Via Postman:
- Import the Postman collection from `src/test/resources/search/e2e/postman/`
- Set `{{base_url}}` to `http://localhost:8080`
- Run: Search Business by Keyword

## What Each Phase Enables

### Phase 1 (Current - Keyword Only)
```json
{
  "criteria": {
    "query": "plumber"  // NOW WORKS ✅
  }
}
```
Requires:
- ✅ `pg_trgm` extension
- ✅ `search_vector` column
- ✅ GIN index on search_vector

### Phase 2 (Keyword + Geo)
```json
{
  "criteria": {
    "query": "plumber"
  },
  "filters": {
    "userLat": 27.7172,      // NOW WORKS ✅
    "userLng": 85.3240,
    "distanceKm": 5
  }
}
```
Requires Phase 1 + :
- ✅ B-tree indexes on latitude/longitude

### Phase 3 (Keyword + Geo + Other Filters)
```json
{
  "criteria": {
    "query": "plumber"
  },
  "filters": {
    "userLat": 27.7172,
    "userLng": 85.3240,
    "distanceKm": 5,
    "verifiedOnly": true,    // ALREADY WORKS ✅
    "minRating": 4.0,        // ALREADY WORKS ✅
    "municipalityId": 12345  // ALREADY WORKS ✅
  }
}
```
Requires Phase 2 (no additional schema changes needed).

## SQL Query Generated (For Your Reference)

When a user searches for "plumber" near Kathmandu:

```sql
-- The BusinessSearchQueryBuilder generates this SQL:
SELECT 
  b.id, 
  b.name, 
  b.vertical_id, 
  b.average_rating, 
  b.review_count, 
  b.is_kyc_verified, 
  b.status, 
  b.profile_image_url, 
  b.address_field_1, 
  b.address_field_2, 
  b.municipality_id, 
  b.business_hours, 
  b.tole_name, 
  b.ward_number,
  -- FTS ranking column (only when keyword provided):
  ts_rank(b.search_vector, plainto_tsquery('english', 'plumber')) AS search_rank,
  -- Distance column (only when geo filter provided):
  (6371 * acos(
    cos(radians(27.7172)) * cos(radians(b.latitude::float)) * 
    cos(radians(b.longitude::float) - radians(85.3240)) + 
    sin(radians(27.7172)) * sin(radians(b.latitude::float))
  )) AS distance_km

FROM business_master b

WHERE 
  -- Always applied (in all searches):
  b.status = 'PUBLISHED' 
  
  -- Applied when keyword provided (uses search_vector GIN index):
  AND (
    b.search_vector @@ plainto_tsquery('english', 'plumber') 
    OR b.name % 'plumber'
  )
  
  -- Applied when geo filter provided (uses lat/lng B-tree indexes):
  AND b.latitude BETWEEN 27.716 AND 27.718
  AND b.longitude BETWEEN 85.321 AND 85.327

-- Sorted by search relevance:
ORDER BY search_rank DESC

-- Paginated:
LIMIT 20 OFFSET 0;
```

**Index Usage:**
- ✅ `idx_business_search_vector_gin` → Fast FTS matching
- ✅ `idx_business_name_trigram` → Fuzzy fallback
- ✅ `idx_business_latitude` → Geo filtering
- ✅ `idx_business_latitude_longitude` → Combined geo queries

## File Location Reference

| File | Purpose |
|------|---------|
| `src/main/resources/db/migration/V22__search_init_schema.sql` | **Database migration (YOU'LL RUN THIS)** |
| `src/main/java/com/nifilili/search/query/BusinessSearchQueryBuilder.java` | Query building logic |
| `src/main/java/com/nifilili/search/repository/BusinessSearchRepository.java` | Query execution |
| `src/main/java/com/nifilili/search/service/BusinessSearchService.java` | Orchestration |
| `src/main/java/com/nifilili/search/mapper/BusinessSearchResultMapper.java` | Result mapping |

## Troubleshooting

### Issue: Migration file not found
**Solution:** Make sure V22__search_init_schema.sql is in:
```
src/main/resources/db/migration/
```

### Issue: Error "GENERATED ALWAYS not supported"
**Cause:** PostgreSQL version < 12
**Solution:** Use trigger-based implementation (see SEARCH_DB_CHANGES.md for alternative)

### Issue: Index creation is slow
**Note:** First-time index creation on a large table can take minutes. This is normal.
**If blocked:** Create index CONCURRENTLY in background:
```sql
CREATE INDEX CONCURRENTLY idx_business_search_vector_gin ON business_master USING GIN (search_vector);
```

### Issue: Keyword search not working after migration
**Debug steps:**
1. Verify column exists: `SELECT search_vector FROM business_master LIMIT 1;`
2. Check for errors in application logs
3. Run: `mvn clean compile` and restart

## Performance Notes

- **First keyword search:** ~500ms (PostgreSQL caches FTS lookups)
- **Subsequent searches:** ~50-100ms (sub-100ms with caching)
- **Index size:** +10-15% to total table size (GIN indexes are compact)
- **Maintenance:** PostgreSQL automatically updates search_vector when business data changes


