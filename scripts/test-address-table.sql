-- Test script to validate address table functionality
-- This script tests the address table with sample data

-- First, let's check if we have some postal codes to reference
SELECT 'Available postal codes for testing:' as test_step;
SELECT postal_code, city, province_code, country_code 
FROM postal_code_reference 
WHERE country_code IN ('MY', 'SG') 
AND status = 'active' 
ORDER BY country_code, postal_code 
LIMIT 10;

-- Create temporary ULID generation function for testing
CREATE OR REPLACE FUNCTION temp_test_ulid() RETURNS CHAR(26) AS $$
DECLARE
    timestamp_part BIGINT;
    random_part TEXT;
    ulid TEXT;
BEGIN
    timestamp_part := EXTRACT(EPOCH FROM NOW()) * 1000;
    random_part := UPPER(SUBSTR(MD5(RANDOM()::TEXT || RANDOM()::TEXT), 1, 16));
    random_part := REPLACE(random_part, 'D', '8');
    random_part := REPLACE(random_part, 'I', '9');
    random_part := REPLACE(random_part, 'L', 'K');
    random_part := REPLACE(random_part, 'O', '0');
    random_part := REPLACE(random_part, 'U', 'V');
    
    ulid := '0' || LPAD(ABS(HASHTEXT(timestamp_part::TEXT))::TEXT, 9, '0') || random_part;
    RETURN SUBSTR(ulid, 1, 26);
END;
$$ LANGUAGE plpgsql;

-- Test 1: Insert a test address using Singapore postal code
INSERT INTO address (
    id, 
    street_line1, 
    street_line2, 
    city, 
    postal_code_id, 
    province_code, 
    country_code, 
    attention,
    created_by
)
SELECT 
    temp_test_ulid(),
    '123 Marina Bay Street',
    'Unit 45-67',
    pcr.city,
    pcr.id,
    pcr.province_code,
    pcr.country_code,
    'Finance Department',
    temp_test_ulid()
FROM postal_code_reference pcr
WHERE pcr.country_code = 'SG' 
AND pcr.postal_code = '018915'
LIMIT 1;

-- Test 2: Insert a test address using Malaysian postal code
INSERT INTO address (
    id, 
    street_line1, 
    city, 
    postal_code_id, 
    province_code, 
    country_code,
    created_by
)
SELECT 
    temp_test_ulid(),
    'No. 88, Jalan Sultan Yahya Petra',
    pcr.city,
    pcr.id,
    pcr.province_code,
    pcr.country_code,
    temp_test_ulid()
FROM postal_code_reference pcr
WHERE pcr.country_code = 'MY' 
AND pcr.postal_code = '15350'
LIMIT 1;

-- Test 3: Verify the addresses were inserted correctly
SELECT 'Test results - Inserted addresses:' as test_step;
SELECT 
    a.id,
    a.street_line1,
    a.street_line2,
    a.city,
    a.postal_code_id,
    a.province_code,
    a.country_code,
    a.attention,
    a.status,
    pcr.postal_code
FROM address a
JOIN postal_code_reference pcr ON pcr.id = a.postal_code_id
WHERE a.created_at >= NOW() - INTERVAL '1 minute'
ORDER BY a.created_at;

-- Test 4: Test the indexes are being used
EXPLAIN (ANALYZE, BUFFERS) 
SELECT * FROM address 
WHERE postal_code_id = (
    SELECT id FROM postal_code_reference 
    WHERE country_code = 'SG' 
    AND postal_code = '018915' 
    LIMIT 1
);

-- Test 5: Test regional reporting index
EXPLAIN (ANALYZE, BUFFERS)
SELECT country_code, province_code, COUNT(*) as address_count
FROM address 
WHERE country_code = 'SG' 
AND status = 'active'
GROUP BY country_code, province_code;

-- Test 6: Validate constraints work
SELECT 'Testing constraint validations:' as test_step;

-- This should fail due to invalid ULID format
DO $$
BEGIN
    BEGIN
        INSERT INTO address (id, street_line1, postal_code_id, province_code, country_code, created_by)
        VALUES ('invalid-ulid', 'Test Street', 
               (SELECT id FROM postal_code_reference WHERE country_code = 'SG' LIMIT 1),
               'SG', 'SG', temp_test_ulid());
        RAISE NOTICE 'ERROR: Invalid ULID constraint did not trigger';
    EXCEPTION
        WHEN check_violation THEN
            RAISE NOTICE 'SUCCESS: Invalid ULID constraint working correctly';
    END;
END $$;

-- This should fail due to empty street_line1
DO $$
BEGIN
    BEGIN
        INSERT INTO address (id, street_line1, postal_code_id, province_code, country_code, created_by)
        VALUES (temp_test_ulid(), '   ', 
               (SELECT id FROM postal_code_reference WHERE country_code = 'SG' LIMIT 1),
               'SG', 'SG', temp_test_ulid());
        RAISE NOTICE 'ERROR: Empty street_line1 constraint did not trigger';
    EXCEPTION
        WHEN check_violation THEN
            RAISE NOTICE 'SUCCESS: Empty street_line1 constraint working correctly';
    END;
END $$;

-- Test 7: Clean up test data
DELETE FROM address WHERE created_at >= NOW() - INTERVAL '1 minute';

-- Drop temporary function
DROP FUNCTION IF EXISTS temp_test_ulid();

SELECT 'Address table testing completed successfully!' as test_result;