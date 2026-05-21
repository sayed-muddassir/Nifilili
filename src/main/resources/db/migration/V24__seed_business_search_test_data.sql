-- V23__seed_search_test_data.sql
-- ADMIN_SEEDED data for search module testing (keyword, trigram, geo, filters)
-- Defaults used:
--  - Postgres >= 12 (GENERATED columns supported)
--  - Deterministic IDs for repeatable tests
--  - Creates minimal dependent rows: business_verticals, municipality_master, users
--  - Inserts 10 business_master rows covering keyword, typo, geo, verification, ratings

-- Idempotency: delete any previous test rows with these IDs before inserting

-- -------------
-- Cleanup existing test rows (idempotent)
-- -------------
DELETE FROM "business_master" WHERE id BETWEEN 2000 AND 2010;
DELETE FROM "business_verticals" WHERE id IN (1000, 1001);
DELETE FROM "municipality_master" WHERE id IN (1000, 1001);
DELETE FROM "users" WHERE id IN (1000, 1001);

-- -------------
-- Create dependent reference rows
-- -------------
-- Business verticals
INSERT INTO "business_verticals" ("id", "name", "slug", "description", "icon_url", "is_active")
VALUES
  (1000, 'Services', 'services', 'General service providers', '', TRUE),
  (1001, 'Food & Beverage', 'fnb', 'Restaurants and cafes', '', TRUE);

-- Provinces (simple minimal rows)
INSERT INTO "province_master" ("id", "name")
VALUES
  (1, 'Kathmandu');

-- Districts (simple minimal rows)
INSERT INTO "district_master" ("id", "province_id", "name")
VALUES
  (1, 1, 'Kathmandu'),
  (2, 1, 'Lalitpur');

-- Municipalities (simple minimal rows)
INSERT INTO "municipality_master" ("id", "district_id", "name", "type")
VALUES
  (1000, 1, 'Kathmandu', 'municipality'),
  (1001, 1, 'Lalitpur', 'municipality');

-- Minimal users (owners)
INSERT INTO "users" ("id", "username", "email", "password", "name", "created_at", "updated_at")
VALUES
  (1000, 'test_owner_1', 'owner1@example.test', 'x', 'Test Owner 1', now(), now()),
  (1001, 'test_owner_2', 'owner2@example.test', 'x', 'Test Owner 2', now(), now())
ON CONFLICT DO NOTHING;

-- -------------
-- Insert test businesses (IDs 2000..2009)
-- -------------
-- Reference user coords for Kathmandu center: 27.7172, 85.3240

-- Utility note: application code expects status = 'PUBLISHED' for searchable rows

INSERT INTO "business_master" (
  "id", "vertical_id", "owner_user_id", "claimed_by_user_id", "name", "legal_name",
  "municipality_id", "ward_number", "tole_name", "address_field_1", "address_field_2",
  "postal_code", "latitude", "longitude", "contacts", "business_hours", "website",
  "registration_date", "business_summary", "source", "status", "is_claimed",
  "is_kyc_verified", "created_at", "updated_at", "average_rating", "review_count"
) VALUES

-- 1: Exact keyword in name: plumber, published, verified, near center (~0.5km)
(2000, 1000, 1000, NULL, 'ABC Plumbers', 'ABC Plumbers Pvt Ltd', 1000, 10, 'Thamel', 'Thamel Road 1', NULL,
 '44600', 27.7178, 85.3235,
 '{"phone": ["+977-1-1111111"], "email": "abc@plumbers.test"}',
 '{"monday": {"open": "09:00", "close": "18:00"}}', 'https://abc.example.test',
 CURRENT_DATE - INTERVAL '365 days', 'Expert plumbing services and repairs', 'ADMIN_SEEDED', 'PUBLISHED', FALSE, TRUE, now(), now(), 4.5, 120),

-- 2: Keyword in legal_name only (plumber), published, unverified, near center (~1.2km)
(2001, 1000, 1001, NULL, 'PlumbRight Services', 'Plumber PlumbRight Pvt', 1000, 5, 'Kanti', 'Kanti Marg', NULL,
 '44600', 27.7125, 85.3200,
 '{"phone": ["+977-1-2222222"]}', '{"monday": {"open": "08:00", "close": "17:00"}}', 'https://plumbright.example.test',
 CURRENT_DATE - INTERVAL '200 days', 'We do plumbing, leaks and installations', 'ADMIN_SEEDED', 'PUBLISHED', FALSE, FALSE, now(), now(), 3.8, 45),

-- 3: Typo test: name is 'Expert Electrician' (for trigram similarity with 'electician'), published, verified
(2002, 1000, 1000, NULL, 'Expert Electrician', 'Expert Electrician Co', 1000, 3, 'Ason', 'Ason Marg', NULL,
 '44600', 27.7160, 85.3220,
 '{"phone": ["+977-1-3333333"]}', '{"monday": {"open": "09:00", "close": "20:00"}}', 'https://expert-electric.example.test',
 CURRENT_DATE - INTERVAL '400 days', 'Electrical services and rewiring', 'ADMIN_SEEDED', 'PUBLISHED', FALSE, TRUE, now(), now(), 4.2, 67),

-- 4: Keyword only in business_summary, published, unverified
(2003, 1000, 1001, NULL, 'Home Fixers', 'Home Fixers Pvt Ltd', 1000, 7, 'Sankhuwasabha', 'Sankhu Road', NULL,
 '44600', 27.7185, 85.3255,
 '{"phone": ["+977-1-4444444"]}', '{"monday": {"open": "10:00", "close": "16:00"}}', 'https://homefix.example.test',
 CURRENT_DATE - INTERVAL '30 days', 'We are plumbers and handymen for hire', 'ADMIN_SEEDED', 'PUBLISHED', FALSE, FALSE, now(), now(), 3.0, 8),

-- 5: Food vertical, pizza keyword in name, published, near center
(2004, 1001, 1000, NULL, 'Pizza Palace', 'Pizza Palace LLC', 1000, 12, 'Thamel', 'Thamel Street', NULL,
 '44600', 27.7164, 85.3248,
 '{"phone": ["+977-1-5555555"]}', '{"monday": {"open": "11:00", "close": "23:00"}}', 'https://pizzapalace.example.test',
 CURRENT_DATE - INTERVAL '800 days', 'The best pizza in town', 'ADMIN_SEEDED', 'PUBLISHED', FALSE, FALSE, now(), now(), 4.6, 320),

-- 6: Far away business (to test geo filtering), published but far (~50km)
(2005, 1000, 1001, NULL, 'FarAway Cafe', 'FarAway Cafe', 1001, 1, 'Pokhara', 'Lakeside', NULL,
 '33700', 28.2096, 83.9856,
 '{"phone": ["+977-61-6666666"]}', '{"monday": {"open": "07:00", "close": "22:00"}}', 'https://faraway.example.test',
 CURRENT_DATE - INTERVAL '900 days', 'Cafe near the lake', 'ADMIN_SEEDED', 'PUBLISHED', FALSE, TRUE, now(), now(), 4.1, 210),

-- 7: Non-published (should NOT appear in searches)
(2006, 1000, 1000, NULL, 'Hidden Handyman', 'Hidden Handyman Co', 1000, 2, 'Gyaneshwor', 'Gyaneshwor Road', NULL,
 '44600', 27.7140, 85.3205,
 '{"phone": ["+977-1-7777777"]}', '{"monday": {"open": "09:00", "close": "18:00"}}', 'https://hidden.example.test',
 CURRENT_DATE - INTERVAL '10 days', 'Not yet published listing', 'ADMIN_SEEDED', 'PENDING', FALSE, FALSE, now(), now(), 2.5, 5),

-- 8: Low rating published business
(2007, 1000, 1001, NULL, 'Budget Plumbing', 'Budget Plumbing Co', 1000, 15, 'Bhotahiti', 'Bhotahiti Lane', NULL,
 '44600', 27.7195, 85.3260,
 '{"phone": ["+977-1-8888888"]}', '{"monday": {"open": "08:00", "close": "20:00"}}', 'https://budgetplumb.example.test',
 CURRENT_DATE - INTERVAL '60 days', 'Affordable plumbing', 'ADMIN_SEEDED', 'PUBLISHED', FALSE, FALSE, now(), now(), 2.8, 9),

-- 9: Missing business_hours (null), published
(2008, 1000, 1000, NULL, 'NoHours Services', 'NoHours Ltd', 1000, 20, 'Chhetrapati', 'Chhetrapati Street', NULL,
 '44600', 27.7180, 85.3210,
 '{"phone": ["+977-1-9999999"]}', NULL, 'https://nohours.example.test',
 CURRENT_DATE - INTERVAL '120 days', 'Services without hours', 'ADMIN_SEEDED', 'PUBLISHED', FALSE, TRUE, now(), now(), 3.9, 14),

-- 10: Partial name for trigram test: 'Electrocity' -> tests typo/partial matching
(2009, 1000, 1001, NULL, 'Electrocity Solutions', 'Electrocity Solutions Pvt', 1000, 9, 'Basantapur', 'Basantapur Square', NULL,
 '44600', 27.7189, 85.3242,
 '{"phone": ["+977-1-1010101"]}', '{"monday": {"open": "09:00", "close": "19:00"}}', 'https://electro.example.test',
 CURRENT_DATE - INTERVAL '150 days', 'Electrical and electronic solutions', 'ADMIN_SEEDED', 'PUBLISHED', FALSE, FALSE, now(), now(), 4.0, 34);

-- -------------
-- Verification convenience queries (run after migration applied)
-- -------------
-- Check inserted businesses:
-- SELECT id, name, status, is_kyc_verified, average_rating FROM "business_master" WHERE id BETWEEN 2000 AND 2010 ORDER BY id;

-- Test FTS: should return rows for 'plumber'
-- SELECT id, name, ts_rank(search_vector, plainto_tsquery('english', 'plumber')) AS rank
-- FROM "business_master" WHERE search_vector @@ plainto_tsquery('english', 'plumber') ORDER BY rank DESC;

-- Test trigram (typo): 'electician' should match 'Expert Electrician' or 'Electrocity' depending on similarity
-- SELECT id, name, similarity(name, 'electician') AS sim FROM "business_master" WHERE name % 'electician' ORDER BY sim DESC LIMIT 10;

-- Test geo bounding box (example lat/lng ~ Kathmandu center)
-- SELECT id, name, latitude, longitude FROM "business_master"
-- WHERE latitude BETWEEN 27.716 AND 27.719 AND longitude BETWEEN 85.321 AND 85.326 ORDER BY id;

-- End of ADMIN_SEEDED script

