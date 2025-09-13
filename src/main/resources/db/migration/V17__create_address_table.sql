-- V17__create_address_table.sql
-- Create address table for storing full postal addresses linked to parties
-- Based on address_sizing.md specifications and business requirements

-- Create the address table with optimized schema
CREATE TABLE address (
    id CHAR(26) PRIMARY KEY,
    street_line1 VARCHAR(255) NOT NULL,
    street_line2 VARCHAR(255),
    city VARCHAR(120),                        -- Nullable, required in some countries
    postal_code_id CHAR(26) NOT NULL,         -- Reference to postal_code_reference.id
    province_code CHAR(5) NOT NULL,
    country_code CHAR(2) NOT NULL,
    attention VARCHAR(120),                   -- Optional contact or department
    status VARCHAR(20) DEFAULT 'active',

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by CHAR(26) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now(),
    updated_by CHAR(26),
    deleted_at TIMESTAMPTZ,
    deleted_by CHAR(26),

    -- ULID format validation (Crockford base32, 26 chars)
    CONSTRAINT address_id_ulid_chk CHECK (id ~ '^[0-7][0-9A-HJKMNP-TV-Z]{25}$'),
    
    -- Foreign key to postal_code_reference for validation and auto-population
    CONSTRAINT fk_address_postal_code 
        FOREIGN KEY (postal_code_id) 
        REFERENCES postal_code_reference (id)
        ON DELETE RESTRICT,
    
    -- Country code format validation (2 uppercase letters)
    CONSTRAINT chk_address_country_code_format 
        CHECK (country_code ~ '^[A-Z]{2}$'),
    
    -- Province code format validation (max 5 chars, alphanumeric)
    CONSTRAINT chk_address_province_code_format 
        CHECK (province_code ~ '^[A-Z0-9]{1,5}$'),
    
    -- Status validation
    CONSTRAINT chk_address_status 
        CHECK (status IN ('active', 'inactive', 'archived')),
    
    -- Street line 1 cannot be empty
    CONSTRAINT chk_address_street_line1_not_empty 
        CHECK (TRIM(street_line1) != '')
);

-- Indexing Strategy based on primary access patterns

-- 1. Postal code lookup - most common access pattern
CREATE INDEX ix_address_postal
    ON address (postal_code_id)
    WHERE status = 'active' AND deleted_at IS NULL;

-- 2. Reporting by province/country - for aggregation and reporting
CREATE INDEX ix_address_region
    ON address (country_code, province_code)
    WHERE status = 'active' AND deleted_at IS NULL;

-- 3. Status and soft delete support for admin queries
CREATE INDEX ix_address_status_active
    ON address (status, deleted_at)
    WHERE deleted_at IS NULL;

-- 4. Audit trail queries
CREATE INDEX ix_address_created_at
    ON address (created_at);

-- 5. Updated audit trail
CREATE INDEX ix_address_updated_at
    ON address (updated_at)
    WHERE updated_at IS NOT NULL;

-- Table and column documentation
COMMENT ON TABLE address IS 'Stores full postal addresses linked to parties (suppliers, customers, studios, etc.) with legal compliance for billing and invoicing. Supports standardized reporting grouped by country and province.';

COMMENT ON COLUMN address.id IS 'Primary key using ULID format for distributed systems';
COMMENT ON COLUMN address.street_line1 IS 'First line of street address (required)';
COMMENT ON COLUMN address.street_line2 IS 'Second line of street address (optional - apartment, suite, etc.)';
COMMENT ON COLUMN address.city IS 'City name (nullable, enforcement varies by country requirements)';
COMMENT ON COLUMN address.postal_code_id IS 'Foreign key reference to postal_code_reference.id for validation and auto-population of geographic data';
COMMENT ON COLUMN address.province_code IS 'Province/state code duplicated from postal_code_reference for efficient reporting without joins';
COMMENT ON COLUMN address.country_code IS 'ISO 3166-1 alpha-2 country code duplicated from postal_code_reference for efficient reporting';
COMMENT ON COLUMN address.attention IS 'Optional attention line for business addresses (department, contact person, etc.)';
COMMENT ON COLUMN address.status IS 'Record status: active (in use), inactive (temporarily disabled), archived (historical)';

COMMENT ON COLUMN address.created_at IS 'Timestamp when the address record was created';
COMMENT ON COLUMN address.created_by IS 'ULID of the user who created this address record';
COMMENT ON COLUMN address.updated_at IS 'Timestamp when the address record was last updated';
COMMENT ON COLUMN address.updated_by IS 'ULID of the user who last updated this address record';
COMMENT ON COLUMN address.deleted_at IS 'Timestamp when the address record was soft-deleted (NULL for active records)';
COMMENT ON COLUMN address.deleted_by IS 'ULID of the user who soft-deleted this address record';

-- Performance and usage notes
COMMENT ON INDEX ix_address_postal IS 'Primary access pattern: lookup addresses by postal code ID for validation and auto-completion';
COMMENT ON INDEX ix_address_region IS 'Reporting access pattern: aggregate addresses by country and province for business intelligence';

-- Expected usage patterns (for reference):
-- 1. Address validation: WHERE postal_code_id = ? AND status = 'active'
-- 2. Regional reporting: WHERE country_code = ? AND province_code = ? AND status = 'active'
-- 3. Entity address lookup: JOIN with customer/supplier tables ON address.id
-- 4. Audit queries: WHERE created_at BETWEEN ? AND ? OR updated_at BETWEEN ? AND ?