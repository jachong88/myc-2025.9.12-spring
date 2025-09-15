-- V19__seed_default_admin.sql
-- Seed a default SUPER_ADMIN user for initial login
-- Adjust the IDs if you prefer different ULIDs. Format must match ULID regex constraint.

-- Known role id for SUPER_ADMIN from V6__rbac_seeds.sql
-- SUPER_ADMIN id: 01HZRB00000000000000000001

-- Choose deterministic ULIDs (26 chars, Crockford base32, first char 0-7)
-- Admin user id:
--   01HZRB90000000000000000001
-- UserRole link id:
--   01HZRB91000000000000000001

-- Insert user if not exists (active rows only considered unique)
INSERT INTO users (id, email, phone, full_name, is_active, country_id, province_id, role_id, created_by, updated_by)
SELECT
  '01HZRB90000000000000000001',                               -- id
  'jachong8@gmail.com',                                        -- email
  NULL,                                                        -- phone
  'System Administrator',                                      -- full_name
  TRUE,                                                        -- is_active
  NULL,                                                        -- country_id
  NULL,                                                        -- province_id
  '01HZRB00000000000000000001',                                -- role_id (mirror SUPER_ADMIN for UI convenience)
  NULL,
  NULL
WHERE NOT EXISTS (
  SELECT 1 FROM users WHERE LOWER(email) = LOWER('jachong8@gmail.com') AND deleted_at IS NULL
);

-- Create user_roles link to SUPER_ADMIN if missing
INSERT INTO user_roles (id, user_id, role_id, created_by)
SELECT
  '01HZRB91000000000000000001',                                -- id
  u.id,                                                        -- user_id
  r.id,                                                        -- role_id (SUPER_ADMIN)
  NULL
FROM roles r
JOIN users u ON LOWER(u.email) = LOWER('jachong8@gmail.com') AND u.deleted_at IS NULL
WHERE r.name = 'SUPER_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );
