-- V18__create_studio_table.sql
-- Create studio table for managing dance studios and their business information
-- Based on studio-add.md wireframe requirements

-- Create the studio table
CREATE TABLE studio (
    id CHAR(26) PRIMARY KEY,
    
    -- Studio identification
    name VARCHAR(255) NOT NULL,                    -- Studio name (required)
    code VARCHAR(50) NOT NULL,                     -- Studio code (required, business identifier)
    
    -- Contact information
    phone VARCHAR(20),                             -- Phone number (optional)
    email VARCHAR(320),                            -- Email address (optional but must be unique if provided)
    
    -- Company information
    company_name VARCHAR(255) NOT NULL,            -- Company name (required)
    company_registration_no VARCHAR(100),          -- Company registration number (optional)
    
    -- Address relationship
    address_id CHAR(26) NOT NULL,                  -- Foreign key to address table (required)
    
    -- Ownership
    owner_id CHAR(26) NOT NULL,                    -- Foreign key to users table (required)
    
    -- Additional information
    note TEXT,                                     -- Notes/comments (optional text area)
    
    -- Status and soft delete
    status VARCHAR(20) DEFAULT 'active',
    
    -- Standard audit fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by CHAR(26) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now(),
    updated_by CHAR(26),
    deleted_at TIMESTAMPTZ,
    deleted_by CHAR(26),

    -- Constraints
    -- ULID format validation (Crockford base32, 26 chars)
    CONSTRAINT studio_id_ulid_chk CHECK (id ~ '^[0-7][0-9A-HJKMNP-TV-Z]{25}$'),
    
    -- Foreign key to address table
    CONSTRAINT fk_studio_address 
        FOREIGN KEY (address_id) 
        REFERENCES address (id)
        ON DELETE RESTRICT,
    
    -- Foreign key to users table (owner)
    CONSTRAINT fk_studio_owner 
        FOREIGN KEY (owner_id) 
        REFERENCES users (id)
        ON DELETE RESTRICT,
    
    -- Studio name cannot be empty
    CONSTRAINT chk_studio_name_not_empty 
        CHECK (TRIM(name) != ''),
    
    -- Studio code cannot be empty and should be alphanumeric
    CONSTRAINT chk_studio_code_format 
        CHECK (TRIM(code) != '' AND code ~ '^[A-Z0-9_-]+$'),
    
    -- Company name cannot be empty
    CONSTRAINT chk_studio_company_name_not_empty 
        CHECK (TRIM(company_name) != ''),
    
    -- Email format validation (if provided)
    CONSTRAINT chk_studio_email_format 
        CHECK (email IS NULL OR email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    
    -- Phone format validation (if provided) - allows international formats
    CONSTRAINT chk_studio_phone_format 
        CHECK (phone IS NULL OR phone ~ '^[\+]?[0-9\s\-\(\)]{7,20}$'),
    
    -- Status validation
    CONSTRAINT chk_studio_status 
        CHECK (status IN ('active', 'inactive', 'suspended', 'archived'))
);

-- Indexing Strategy for Studio table

-- 1. Unique constraints for business rules
-- Email uniqueness (only for active, non-deleted records with email)
CREATE UNIQUE INDEX ux_studio_email_active
    ON studio (LOWER(email))
    WHERE deleted_at IS NULL AND status = 'active' AND email IS NOT NULL;

-- Studio code uniqueness (only for active, non-deleted records)
CREATE UNIQUE INDEX ux_studio_code_active
    ON studio (UPPER(code))
    WHERE deleted_at IS NULL AND status = 'active';

-- 2. Lookup by owner (for scope-based queries)
CREATE INDEX ix_studio_owner
    ON studio (owner_id)
    WHERE deleted_at IS NULL;

-- 3. Lookup by address (for geographic queries)
CREATE INDEX ix_studio_address
    ON studio (address_id)
    WHERE deleted_at IS NULL;

-- 4. Status and soft delete support
CREATE INDEX ix_studio_status_active
    ON studio (status, deleted_at)
    WHERE deleted_at IS NULL;

-- 5. Audit trail queries
CREATE INDEX ix_studio_created_at
    ON studio (created_at);

-- 6. Search by name (for autocomplete/search functionality)
CREATE INDEX ix_studio_name_search
    ON studio USING gin(to_tsvector('simple', name))
    WHERE deleted_at IS NULL AND status = 'active';

-- 7. Company information search
CREATE INDEX ix_studio_company_name
    ON studio (company_name)
    WHERE deleted_at IS NULL;

-- Table and column documentation
COMMENT ON TABLE studio IS 'Stores dance studio business information including company details, contact info, and address. Links to owners (users) and physical addresses for legal compliance and business management.';

COMMENT ON COLUMN studio.id IS 'Primary key using ULID format for distributed systems';
COMMENT ON COLUMN studio.name IS 'Studio display name as shown to customers and in the system';
COMMENT ON COLUMN studio.code IS 'Business identifier code for internal reference (uppercase, alphanumeric, unique per active studio)';
COMMENT ON COLUMN studio.phone IS 'Primary contact phone number in international format';
COMMENT ON COLUMN studio.email IS 'Primary contact email address (must be unique across active studios)';
COMMENT ON COLUMN studio.company_name IS 'Legal company name for business registration and invoicing purposes';
COMMENT ON COLUMN studio.company_registration_no IS 'Official company registration number from government authorities';
COMMENT ON COLUMN studio.address_id IS 'Foreign key reference to address table containing the studio''s physical location';
COMMENT ON COLUMN studio.owner_id IS 'Foreign key reference to users table indicating the studio owner/primary contact';
COMMENT ON COLUMN studio.note IS 'Additional notes, comments, or special instructions about the studio';
COMMENT ON COLUMN studio.status IS 'Studio operational status: active (operating), inactive (temporarily closed), suspended (blocked), archived (historical)';

COMMENT ON COLUMN studio.created_at IS 'Timestamp when the studio record was created';
COMMENT ON COLUMN studio.created_by IS 'ULID of the user who created this studio record';
COMMENT ON COLUMN studio.updated_at IS 'Timestamp when the studio record was last updated';
COMMENT ON COLUMN studio.updated_by IS 'ULID of the user who last updated this studio record';
COMMENT ON COLUMN studio.deleted_at IS 'Timestamp when the studio record was soft-deleted (NULL for active records)';
COMMENT ON COLUMN studio.deleted_by IS 'ULID of the user who soft-deleted this studio record';

-- Index documentation
COMMENT ON INDEX ux_studio_email_active IS 'Ensures email uniqueness across active studios for business communications';
COMMENT ON INDEX ux_studio_code_active IS 'Ensures studio code uniqueness for business identification and internal referencing';
COMMENT ON INDEX ix_studio_owner IS 'Supports owner-based queries for scope and permission filtering';
COMMENT ON INDEX ix_studio_address IS 'Supports location-based queries and geographic reporting';
COMMENT ON INDEX ix_studio_name_search IS 'Full-text search index for studio name autocomplete and search functionality';

-- Expected usage patterns (for reference):
-- 1. Create studio: INSERT with address_id from postal code lookup
-- 2. Find studios by owner: WHERE owner_id = ? AND status = 'active'
-- 3. Search studios: WHERE to_tsvector('simple', name) @@ plainto_tsquery('simple', ?)
-- 4. Geographic queries: JOIN address ON studio.address_id = address.id WHERE address.country_code = ?
-- 5. Email validation: WHERE email = ? AND status = 'active' AND deleted_at IS NULL