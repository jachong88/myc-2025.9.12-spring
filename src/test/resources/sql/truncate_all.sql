-- Truncate mutable application tables only (keep reference data seeded by Flyway)
-- Do NOT truncate roles, permissions, role_permissions, country, province
TRUNCATE TABLE
  user_roles,
  user_scope,
  users
RESTART IDENTITY CASCADE;
