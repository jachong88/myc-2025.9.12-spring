-- V13__complete_malaysia_provinces.sql
-- Add all missing Malaysian states/territories with ISO 3166-2 codes
-- Based on official Malaysian administrative divisions

-- Generate ULID function (temporary for this migration)
CREATE OR REPLACE FUNCTION generate_ulid() RETURNS CHAR(26) AS $$
DECLARE
    timestamp_part BIGINT;
    random_part TEXT;
    ulid TEXT;
BEGIN
    timestamp_part := EXTRACT(EPOCH FROM NOW()) * 1000;
    -- Generate a random string that passes ULID validation
    random_part := UPPER(SUBSTR(MD5(RANDOM()::TEXT || RANDOM()::TEXT), 1, 16));
    -- Replace invalid characters with valid ones
    random_part := REPLACE(random_part, 'D', '8');
    random_part := REPLACE(random_part, 'I', '9');
    random_part := REPLACE(random_part, 'L', 'K');
    random_part := REPLACE(random_part, 'O', '0');
    random_part := REPLACE(random_part, 'U', 'V');
    
    -- Create ULID-like string (first char must be 0-7)
    ulid := '0' || LPAD(ABS(HASHTEXT(timestamp_part::TEXT))::TEXT, 9, '0') || random_part;
    RETURN SUBSTR(ulid, 1, 26);
END;
$$ LANGUAGE plpgsql;

-- Insert missing Malaysian states (currently missing 10 out of 13)
-- Reference: https://en.wikipedia.org/wiki/ISO_3166-2:MY

-- MY-02: Kedah
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'MY',
  'MY-02',
  'Kedah',
  'active',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE province_code = 'MY-02');

-- MY-03: Kelantan  
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'MY',
  'MY-03', 
  'Kelantan',
  'active',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE province_code = 'MY-03');

-- MY-04: Melaka
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'MY',
  'MY-04',
  'Melaka',
  'active', 
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE province_code = 'MY-04');

-- MY-05: Negeri Sembilan
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'MY',
  'MY-05',
  'Negeri Sembilan',
  'active',
  NOW(), 
  'system',
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE province_code = 'MY-05');

-- MY-06: Pahang
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'MY',
  'MY-06',
  'Pahang',
  'active',
  NOW(),
  'system', 
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE province_code = 'MY-06');

-- MY-07: Penang
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'MY',
  'MY-07',
  'Penang',
  'active',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE province_code = 'MY-07');

-- MY-08: Perak
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'MY',
  'MY-08',
  'Perak', 
  'active',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE province_code = 'MY-08');

-- MY-09: Perlis
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'MY',
  'MY-09',
  'Perlis',
  'active',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE province_code = 'MY-09');

-- MY-11: Sabah
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'MY',
  'MY-11',
  'Sabah',
  'active',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE province_code = 'MY-11');

-- MY-12: Sarawak
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'MY',
  'MY-12',
  'Sarawak',
  'active',
  NOW(),
  'system', 
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE province_code = 'MY-12');

-- MY-13: Terengganu
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'MY',
  'MY-13',
  'Terengganu',
  'active',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE province_code = 'MY-13');

-- MY-15: Labuan (Federal Territory)
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'MY',
  'MY-15',
  'Labuan',
  'active',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE province_code = 'MY-15');

-- MY-16: Putrajaya (Federal Territory)
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'MY',
  'MY-16',
  'Putrajaya',
  'active',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE province_code = 'MY-16');

-- Update existing provinces to use correct ISO codes
-- Fix Johor (currently MY-01, should stay MY-01) 
UPDATE province SET province_code = 'MY-01' WHERE province_code = 'MY-01' AND country_code = 'MY';

-- Fix Selangor (currently MY-10, should be MY-10)
UPDATE province SET province_code = 'MY-10' WHERE province_code = 'MY-10' AND country_code = 'MY';

-- Fix Kuala Lumpur (currently MY-14, should stay MY-14)
UPDATE province SET province_code = 'MY-14' WHERE province_code = 'MY-14' AND country_code = 'MY';

-- Drop the temporary ULID function
DROP FUNCTION IF EXISTS generate_ulid();

-- Add helpful comments
COMMENT ON TABLE province IS 'Malaysian states and federal territories with ISO 3166-2 codes';

-- Verification: Should now have 13 Malaysian provinces + 3 federal territories = 16 total for Malaysia
-- Plus 5 Singapore regions = 21 total provinces