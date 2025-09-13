-- V10__postal_code_reference_table.sql
-- Create postal_code_reference table with optimized schema and indexing strategy
-- Based on postal_code_reference_sizing.md specifications

-- Create the postal_code_reference table
CREATE TABLE postal_code_reference (
    id CHAR(26) PRIMARY KEY,
    postal_code VARCHAR(16) NOT NULL,
    city VARCHAR(120),                    -- Nullable, populated if dataset provides it
    province_code CHAR(5) NOT NULL,
    country_code CHAR(2) NOT NULL,
    status VARCHAR(20) DEFAULT 'active',

    created_at TIMESTAMP NOT NULL,
    created_by CHAR(26) NOT NULL,
    updated_at TIMESTAMP,
    updated_by CHAR(26),
    deleted_at TIMESTAMP,
    deleted_by CHAR(26)
);

-- Indexing Strategy based on primary access patterns

-- 1. Ensure uniqueness within a country (exact lookup)
CREATE UNIQUE INDEX ux_postal_country
  ON postal_code_reference (postal_code, country_code)
  WHERE status = 'active' AND deleted_at IS NULL;

-- 2. Autocomplete performance (prefix search)
CREATE INDEX ix_postal_autocomplete
  ON postal_code_reference (country_code, postal_code)
  WHERE status = 'active' AND deleted_at IS NULL;

-- 3. Reporting by province/country
CREATE INDEX ix_province_country
  ON postal_code_reference (province_code, country_code)
  WHERE status = 'active' AND deleted_at IS NULL;

-- 4. Soft delete support (for admin queries)
CREATE INDEX ix_postal_deleted_at
  ON postal_code_reference (deleted_at)
  WHERE deleted_at IS NOT NULL;

-- 5. Audit trail queries
CREATE INDEX ix_postal_created_at
  ON postal_code_reference (created_at);

-- Add foreign key constraints to maintain referential integrity
-- Note: We cannot add a direct FK to country (code) because the unique index is on LOWER(code)
-- Instead, we'll add application-level validation for referential integrity
-- ALTER TABLE postal_code_reference 
--   ADD CONSTRAINT fk_postal_country 
--   FOREIGN KEY (country_code) 
--   REFERENCES country (code) 
--   ON DELETE RESTRICT;

-- Add a check constraint to ensure country_code format is valid (2 uppercase letters)
ALTER TABLE postal_code_reference 
  ADD CONSTRAINT chk_country_code_format 
  CHECK (country_code ~ '^[A-Z]{2}$');

-- Note: province_code constraint would require aligning province.code with this structure
-- For now, we'll add a comment for future implementation
-- ALTER TABLE postal_code_reference 
--   ADD CONSTRAINT fk_postal_province 
--   FOREIGN KEY (province_code, country_code) 
--   REFERENCES province (code, country_code) 
--   ON DELETE RESTRICT;

-- Add table comment for documentation
COMMENT ON TABLE postal_code_reference IS 'Stores mappings between postal codes and their geographic context (city, province, country) for address validation and auto-completion';
COMMENT ON COLUMN postal_code_reference.postal_code IS 'Postal code in country-specific format (e.g., A1A 1A1 for Canada, 12345 for US)';
COMMENT ON COLUMN postal_code_reference.city IS 'City name, nullable if not provided by dataset';
COMMENT ON COLUMN postal_code_reference.province_code IS 'Province/state code, references province.code';
COMMENT ON COLUMN postal_code_reference.country_code IS 'ISO 3166-1 alpha-2 country code, references country.code';
COMMENT ON COLUMN postal_code_reference.status IS 'Record status: active, inactive, deprecated';