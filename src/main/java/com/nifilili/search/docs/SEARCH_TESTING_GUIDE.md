# Testing Keyword Search After Migration

## Endpoint Details

### POST `/api/v1/public/search/businesses`
Location: `SearchController.searchBusinesses()`
- **Access:** Public (no auth required)
- **Response:** Paginated list of matched businesses with location, rating, and verification status

---

## Test Case 1: Basic Keyword Search

### Request
```bash
curl -X POST http://localhost:8080/api/v1/public/search/businesses \
  -H "Content-Type: application/json" \
  -d '{
    "criteria": {
      "query": "plumber"
    },
    "page": 0,
    "size": 20
  }'
```

### Expected Response (`200 OK`)
```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "total": 45,
    "page": 0,
    "size": 20,
    "hasNext": true,
    "results": [
      {
        "id": 1001,
        "name": "ABC Plumbers",
        "verticalId": 5,
        "averageRating": "4.5",
        "reviewCount": 120,
        "isKycVerified": true,
        "status": "PUBLISHED",
        "profileImageUrl": "https://...",
        "formattedAddress": "Thamel, Ward 10, Kathmandu",
        "municipalityId": 12345,
        "distanceKm": null,
        "isOpenNow": true
      },
      {
        "id": 1002,
        "name": "Expert Plumbing Services",
        "verticalId": 5,
        "averageRating": "4.2",
        "reviewCount": 89,
        "isKycVerified": false,
        "status": "PUBLISHED",
        "profileImageUrl": "https://...",
        "formattedAddress": "Bhaktapur, Ward 5",
        "municipalityId": 12346,
        "distanceKm": null,
        "isOpenNow": false
      }
    ]
  }
}
```

### What's Being Tested
- ✅ `search_vector` column is populated for businesses with keyword matches
- ✅ `ts_rank()` scoring works (results ordered by relevance)
- ✅ Trigram `%` fallback works for name fuzzy matching
- ✅ Pagination works (page=0, size=20)
- ✅ Only `PUBLISHED` businesses returned

---

## Test Case 2: Keyword Search + Verified Filter

### Request
```bash
curl -X POST http://localhost:8080/api/v1/public/search/businesses \
  -H "Content-Type: application/json" \
  -d '{
    "criteria": {
      "query": "restaurant"
    },
    "filters": {
      "verifiedOnly": true
    },
    "page": 0,
    "size": 10
  }'
```

### Expected Response
Only restaurants with `is_kyc_verified = true` returned.

### What's Being Tested
- ✅ Keyword filtering works
- ✅ Verification filter works (no schema changes needed)
- ✅ Multiple filters can be combined

---

## Test Case 3: Keyword Search + Geo Filter

### Request
```bash
curl -X POST http://localhost:8080/api/v1/public/search/businesses \
  -H "Content-Type: application/json" \
  -d '{
    "criteria": {
      "query": "pizza"
    },
    "filters": {
      "userLat": 27.7172,
      "userLng": 85.3240,
      "distanceKm": 5
    },
    "page": 0,
    "size": 20
  }'
```

### Expected Response
Pizza restaurants within 5km of coordinates (27.7172, 85.3240):

```json
{
  "success": true,
  "data": {
    "total": 12,
    "results": [
      {
        "id": 2001,
        "name": "Tomato Pizza Co",
        "distanceKm": "1.23",
        "formattedAddress": "Thamel, Ward 10, Kathmandu",
        "isOpenNow": true,
        ...
      },
      {
        "id": 2002,
        "name": "Marco Pizza",
        "distanceKm": "2.45",
        "formattedAddress": "Kathmandu, Ward 8",
        "isOpenNow": false,
        ...
      }
    ]
  }
}
```

### What's Being Tested
- ✅ `idx_business_latitude` index used for latitude filtering
- ✅ `idx_business_latitude_longitude` index used for range queries
- ✅ Haversine distance calculation works (`distanceKm` field populated)
- ✅ Results ordered by search relevance (closest pizza places matching "pizza" best)
- ✅ Bounding box pre-filtering works (only businesses in 5km box returned)

---

## Test Case 4: Keyword Variations (Fuzziness)

### Request 1: Typo in keyword
```bash
curl -X POST http://localhost:8080/api/v1/public/search/businesses \
  -H "Content-Type: application/json" \
  -d '{
    "criteria": {
      "query": "electician"  # Typo (should be "electrician")
    },
    "page": 0,
    "size": 10
  }'
```

### Expected Behavior
- Trigram similarity (`%` operator) matches "electrician" despite typo
- Results returned because `b.name % 'electician'` evaluates to true

### Request 2: Partial word (stemming)
```bash
curl -X POST http://localhost:8080/api/v1/public/search/businesses \
  -H "Content-Type: application/json" \
  -d '{
    "criteria": {
      "query": "plumb"  # Partial (stem matching)
    },
    "page": 0,
    "size": 10
  }'
```

### Expected Behavior
- Full-text search (`to_tsvector` + `plainto_tsquery`) matches "plumber" because:
  - "plumber" stemmed = "plumb"
  - "plumb" + stemming rules = matches "plumb" query
- Results returned with plumber-related businesses

### What's Being Tested
- ✅ Trigram fallback works for typos
- ✅ FTS stemming works for partial words
- ✅ English stopword removal (via `to_tsvector('english', ...)`)

---

## Test Case 5: Empty Keyword Search

### Request
```bash
curl -X POST http://localhost:8080/api/v1/public/search/businesses \
  -H "Content-Type: application/json" \
  -d '{
    "criteria": null,
    "filters": {
      "municipalityId": 12345,
      "minRating": 4.0
    },
    "page": 0,
    "size": 20
  }'
```

### Expected Response
All published businesses in municipality 12345 with rating >= 4.0, sorted by ID (no FTS relevance available).

### What's Being Tested
- ✅ Keyword is optional (filters work without it)
- ✅ No FTS @ operator used (search_rank defaults to 0)
- ✅ Other filters still work

---

## Manual SQL Testing (Database Verification)

### Verify search_vector populated
```sql
SELECT COUNT(*) 
FROM business_master 
WHERE search_vector IS NULL AND status = 'PUBLISHED';

-- Should return: 0 (all published businesses have search_vector)
```

### Test FTS query directly
```sql
SELECT b.id, b.name, ts_rank(b.search_vector, query) AS rank
FROM business_master b, 
     plainto_tsquery('english', 'plumber') AS query
WHERE b.status = 'PUBLISHED'
  AND b.search_vector @@ query
ORDER BY rank DESC
LIMIT 10;

-- Should return plumber-related businesses ordered by relevance
```

### Test trigram query directly
```sql
SELECT b.id, b.name, similarity(b.name, 'electician') AS sim
FROM business_master b
WHERE b.name % 'electician'
ORDER BY sim DESC
LIMIT 10;

-- Should return businesses with names similar to "electician"
```

### Test geo query directly
```sql
SELECT b.id, b.name,
       (6371 * acos(
         cos(radians(27.7172)) * cos(radians(b.latitude::float)) * 
         cos(radians(b.longitude::float) - radians(85.3240)) + 
         sin(radians(27.7172)) * sin(radians(b.latitude::float))
       )) AS distance_km
FROM business_master b
WHERE b.latitude BETWEEN 27.716 AND 27.718
  AND b.longitude BETWEEN 85.321 AND 85.327
ORDER BY distance_km ASC
LIMIT 10;

-- Should return businesses within 5km bounding box
```

### Verify indexes exist
```sql
SELECT indexname 
FROM pg_indexes 
WHERE tablename = 'business_master'
ORDER BY indexname;

-- Should include:
-- idx_business_search_vector_gin
-- idx_business_name_trigram
-- idx_business_latitude
-- idx_business_latitude_longitude
-- idx_business_status_published
```

### Check index sizes
```sql
SELECT 
  indexrelname,
  pg_size_pretty(pg_relation_size(indexrelid)) AS size
FROM pg_stat_user_indexes
WHERE relname = 'business_master'
ORDER BY pg_relation_size(indexrelid) DESC;

-- Shows how much storage each index is using
```

---

## Expected Performance

| Operation | Time | Condition |
|-----------|------|-----------|
| Keyword only (`"plumber"`) | 50-150ms | First: cold cache, warm: <50ms |
| Keyword + geo + filters | 100-300ms | B-tree indexes on lat/lng used |
| Geo + filters only (no keyword) | 20-50ms | GIN index not engaged, pure B-tree |
| Pagination page 0 vs page 100 | Similar | LIMIT/OFFSET both fast with indexes |

---

## Debugging: If Search Returns No Results

### Step 1: Check migration applied
```sql
-- In PostgreSQL:
\d business_master

-- Look for: search_vector column with type `tsvector`
-- If not present, Flyway migration didn't run
```

### Step 2: Check data exists
```sql
SELECT COUNT(*) FROM business_master WHERE status = 'PUBLISHED';

-- Should return > 0
```

### Step 3: Check search_vector is populated
```sql
SELECT COUNT(*) 
FROM business_master 
WHERE status = 'PUBLISHED' AND search_vector IS NOT NULL;

-- Should return same as total published count
```

### Step 4: Test FTS manually
```sql
SELECT id, name 
FROM business_master
WHERE search_vector @@ plainto_tsquery('english', 'plumber')
LIMIT 5;

-- If this returns nothing, check if any businesses have "plumber" in name/legal_name/summary
SELECT id, name, legal_name, business_summary
FROM business_master
WHERE status = 'PUBLISHED' 
  AND (name ILIKE '%plumb%' OR legal_name ILIKE '%plumb%' OR business_summary ILIKE '%plumb%')
LIMIT 5;
```

### Step 5: Check application logs
```bash
# Look for error messages:
tail -f logs/spring.log | grep -i "search\|fts\|tsvector"
```

---

## Integration Testing (Unit Tests)

Location: `src/test/java/com/nifilili/search/`

Example test to add:

```java
@Test
void searchByKeyword_WhenKeywordProvided_ShouldReturnRankedResults() {
    // Given
    SearchRequest request = SearchRequest.builder()
            .criteria(SearchCriteria.builder().query("plumber").build())
            .page(0)
            .size(20)
            .build();
    
    // When
    SearchResponse response = businessSearchService.search(request);
    
    // Then
    assertThat(response.getTotal()).isGreaterThan(0);
    assertThat(response.getResults())
            .allMatch(item -> item.getName().toLowerCase().contains("plumb"));
}
```

---

## Success Criteria

Once the migration is applied and tested, you should be able to:

✅ Search by keyword with relevance ranking
✅ Combine keyword search with geo filtering
✅ Combine keyword search with other filters (verified, rating, municipality)
✅ Get typo-tolerant results (trigram similarity)
✅ Get stem-aware results (plumber ← plumb)
✅ See `distanceKm` populated for geo searches
✅ See results sorted by relevance when keyword provided
✅ See sub-200ms response times for reasonable result sets


