-- V11__province_use_country_code.sql
-- Change province table to use country_code instead of country_id for better performance and consistency

-- Step 1: Add the new country_code column
ALTER TABLE province ADD COLUMN country_code CHAR(2);

-- Step 2: Populate country_code from existing country_id relationships
UPDATE province 
SET country_code = c.code 
FROM country c 
WHERE province.country_id = c.id;

-- Step 3: Make country_code NOT NULL after data migration
ALTER TABLE province ALTER COLUMN country_code SET NOT NULL;

-- Step 4: Drop the old foreign key constraint
ALTER TABLE province DROP CONSTRAINT IF EXISTS province_country_fk;

-- Step 5: Create new foreign key constraint to country.code
-- Note: Cannot add direct FK because unique index is on LOWER(code), not code
-- Using application-level validation instead
-- ALTER TABLE province 
-- ADD CONSTRAINT province_country_code_fk 
-- FOREIGN KEY (country_code) REFERENCES country(code) ON DELETE RESTRICT;

-- Add check constraint to ensure country_code format is valid
ALTER TABLE province 
ADD CONSTRAINT chk_province_country_code_format 
CHECK (country_code ~ '^[A-Z]{2}$');

-- Step 6: Update indexes to use country_code instead of country_id
-- Drop old unique index
DROP INDEX IF EXISTS province_country_code_uq_active;

-- Create new unique index with country_code
CREATE UNIQUE INDEX province_country_code_code_uq_active
  ON province (country_code, code)
  WHERE deleted_at IS NULL AND status = 'active';

-- Drop old country_id index
DROP INDEX IF EXISTS province_country_id_idx;

-- Create new country_code index
CREATE INDEX province_country_code_idx ON province (country_code);

-- Step 7: Remove the old country_id column
ALTER TABLE province DROP COLUMN country_id;

-- Step 8: Add comment for documentation
COMMENT ON COLUMN province.country_code IS 'ISO 3166-1 alpha-2 country code, references country.code for better performance';