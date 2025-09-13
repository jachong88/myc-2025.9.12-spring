-- Quick verification queries for Singapore postal code data

-- Check total count of Singapore postal codes
SELECT COUNT(*) as singapore_postal_codes
FROM postal_code_reference 
WHERE country_code = 'SG';

-- Check Singapore province was added correctly
SELECT id, code, name, local_name, country_code, type, is_active 
FROM province 
WHERE code = 'SG';

-- Sample of Singapore postal codes
SELECT postal_code, city, province_code, country_code, status
FROM postal_code_reference 
WHERE country_code = 'SG' 
ORDER BY postal_code 
LIMIT 10;

-- Check postal code distribution by first two digits (sectors)
SELECT LEFT(postal_code, 2) as sector, COUNT(*) as count
FROM postal_code_reference 
WHERE country_code = 'SG' 
GROUP BY LEFT(postal_code, 2) 
ORDER BY LEFT(postal_code, 2)
LIMIT 20;

-- Check for any duplicates that might have been ignored
SELECT postal_code, COALESCE(city, 'NULL') as city, COUNT(*) as duplicates
FROM postal_code_reference 
WHERE country_code = 'SG'
GROUP BY postal_code, city
HAVING COUNT(*) > 1
ORDER BY duplicates DESC, postal_code;

-- Overall statistics
SELECT 
    'Singapore' as country,
    COUNT(*) as total_postal_codes,
    COUNT(DISTINCT postal_code) as unique_postal_codes,
    COUNT(DISTINCT city) as unique_cities
FROM postal_code_reference 
WHERE country_code = 'SG';