-- V1__init.sql — baseline schema for users, country, province
-- Postgres

-- Countries
CREATE TABLE country (
  id            CHAR(26) PRIMARY KEY,
  code2         CHAR(2)    NOT NULL,
  code3         CHAR(3)    NOT NULL,
  name          VARCHAR(100) NOT NULL,

  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at    TIMESTAMPTZ NULL,

  created_by    CHAR(26) NULL,
  updated_by    CHAR(26) NULL,

  -- Optional: enforce ULID format (Crockford base32, 26 chars)
  CONSTRAINT country_id_ulid_chk CHECK (id ~ '^[0-7][0-9A-HJKMNP-TV-Z]{25}$')
);

-- Uniques only on active (non-deleted) rows
CREATE UNIQUE INDEX country_code2_uq_active ON country (code2) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX country_code3_uq_active ON country (code3) WHERE deleted_at IS NULL;

-- Helpful lookups
CREATE INDEX country_name_idx ON country (name);


-- Provinces / States
CREATE TABLE province (
  id            CHAR(26) PRIMARY KEY,
  country_id    CHAR(26) NOT NULL,
  code          VARCHAR(10) NOT NULL,    -- ISO 3166-2 code (without country prefix if preferred)
  name          VARCHAR(100) NOT NULL,

  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at    TIMESTAMPTZ NULL,

  created_by    CHAR(26) NULL,
  updated_by    CHAR(26) NULL,

  CONSTRAINT province_id_ulid_chk CHECK (id ~ '^[0-7][0-9A-HJKMNP-TV-Z]{25}$'),
  CONSTRAINT province_country_fk FOREIGN KEY (country_id) REFERENCES country(id)
);

-- Uniques only on active rows (per country)
CREATE UNIQUE INDEX province_country_code_uq_active
  ON province (country_id, code)
  WHERE deleted_at IS NULL;

-- Helpful lookups
CREATE INDEX province_country_id_idx ON province (country_id);
CREATE INDEX province_name_idx ON province (name);


-- Users
CREATE TABLE users (
  id            CHAR(26) PRIMARY KEY,

  email         VARCHAR(320) NULL,
  phone         VARCHAR(20)  NULL,
  full_name     VARCHAR(255) NULL,
  is_active     BOOLEAN      NOT NULL DEFAULT TRUE,

  -- Optional foreign keys to location (logical reference; can be null)
  country_id    CHAR(26) NULL,
  province_id   CHAR(26) NULL,

  -- Reserve for future role links (no FK yet)
  role_id       CHAR(26) NULL,

  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at    TIMESTAMPTZ NULL,

  created_by    CHAR(26) NULL,
  updated_by    CHAR(26) NULL,

  CONSTRAINT users_id_ulid_chk CHECK (id ~ '^[0-7][0-9A-HJKMNP-TV-Z]{25}$')
  -- FKs can be added later when reference data populated:
  -- , CONSTRAINT users_country_fk  FOREIGN KEY (country_id)  REFERENCES country(id)
  -- , CONSTRAINT users_province_fk FOREIGN KEY (province_id) REFERENCES province(id)
);

-- Unique constraints only on active (non-deleted) rows
CREATE UNIQUE INDEX users_email_uq_active
  ON users (LOWER(email))
  WHERE deleted_at IS NULL AND email IS NOT NULL;

CREATE UNIQUE INDEX users_phone_uq_active
  ON users (phone)
  WHERE deleted_at IS NULL AND phone IS NOT NULL;

-- Helpful lookups
CREATE INDEX users_country_id_idx  ON users (country_id);
CREATE INDEX users_province_id_idx ON users (province_id);
CREATE INDEX users_role_id_idx     ON users (role_id);
