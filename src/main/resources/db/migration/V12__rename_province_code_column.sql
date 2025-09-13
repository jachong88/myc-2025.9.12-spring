-- V12__rename_province_code_column.sql
-- Rename province.code to province.province_code for consistency with postal_code_reference table

-- Step 1: Rename the column
ALTER TABLE province RENAME COLUMN code TO province_code;

-- Step 2: Update the unique index to use new column name
DROP INDEX IF EXISTS province_country_code_code_uq_active;
CREATE UNIQUE INDEX province_country_province_code_uq_active
  ON province (country_code, province_code)
  WHERE deleted_at IS NULL AND status = 'active';

-- Step 3: Add column comment for documentation
COMMENT ON COLUMN province.province_code IS 'Province/state code in ISO 3166-2 format (e.g., MY-01, SG-01)';

-- Note: No need to update postal_code_reference foreign key since it already uses province_code column name
-- The relationship will work seamlessly: province.province_code = postal_code_reference.province_code