-- V15__add_singapore_province.sql
-- Add Singapore as a province entry (Singapore is a city-state)
-- Based on ISO 3166-1 for Singapore

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

-- Insert Singapore province entry
INSERT INTO province (id, country_code, province_code, name, status, created_at, created_by, updated_at)
SELECT 
  generate_ulid(),
  'SG',
  'SG',
  'Singapore',
  'active',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM province WHERE country_code = 'SG' AND province_code = 'SG');

-- Drop temporary function
DROP FUNCTION IF EXISTS generate_ulid();

-- Add comment for documentation
COMMENT ON TABLE province IS 'Updated to include Singapore as a city-state province entry';

-- Verification query (commented out for migration)
-- SELECT * FROM province WHERE country_code = 'SG';