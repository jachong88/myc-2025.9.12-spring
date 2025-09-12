-- V4__align_country_province_to_spec.sql
-- Align schema with data dictionary for Country and Province (breaking changes)

-- 1) Country: add new columns
ALTER TABLE country ADD COLUMN code VARCHAR(2);
ALTER TABLE country ADD COLUMN currency VARCHAR(3);
ALTER TABLE country ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE country ADD COLUMN deleted_by CHAR(26) NULL;

-- Backfill code from existing code2 and set currency for known seeds
UPDATE country SET code = code2 WHERE code IS NULL;
UPDATE country SET currency = CASE
  WHEN code2 = 'SG' THEN 'SGD'
  WHEN code2 = 'MY' THEN 'MYR'
  ELSE currency
END
WHERE currency IS NULL;

-- Enforce NOT NULL for code and currency after backfill
ALTER TABLE country ALTER COLUMN code SET NOT NULL;
ALTER TABLE country ALTER COLUMN currency SET NOT NULL;

-- Replace unique indexes to use new country.code (case-insensitive)
DROP INDEX IF EXISTS country_code2_uq_active;
DROP INDEX IF EXISTS country_code3_uq_active;
CREATE UNIQUE INDEX country_code_uq_active ON country (LOWER(code)) WHERE deleted_at IS NULL;

-- Remove legacy columns
ALTER TABLE country DROP COLUMN IF EXISTS code2;
ALTER TABLE country DROP COLUMN IF EXISTS code3;

-- 2) Province: add status and deleted_by
ALTER TABLE province ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE province ADD COLUMN deleted_by CHAR(26) NULL;

-- 3) Users: add deleted_by for consistency with BaseEntity
ALTER TABLE users ADD COLUMN deleted_by CHAR(26) NULL;
