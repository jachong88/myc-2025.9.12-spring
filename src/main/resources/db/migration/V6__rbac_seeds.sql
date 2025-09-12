-- V6__rbac_seeds.sql
-- Seed base roles and a minimal set of permissions for USER resource

-- Roles
INSERT INTO roles (id, name, created_by)
SELECT '01HZRB00000000000000000001', 'SUPER_ADMIN', NULL
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'SUPER_ADMIN');

INSERT INTO roles (id, name, created_by)
SELECT '01HZRB00000000000000000002', 'HQ', NULL
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'HQ');

INSERT INTO roles (id, name, created_by)
SELECT '01HZRB00000000000000000003', 'COUNTRY_MANAGER', NULL
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'COUNTRY_MANAGER');

INSERT INTO roles (id, name, created_by)
SELECT '01HZRB00000000000000000004', 'PROVINCE_MANAGER', NULL
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'PROVINCE_MANAGER');

INSERT INTO roles (id, name, created_by)
SELECT '01HZRB00000000000000000005', 'STUDIO_OWNER', NULL
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'STUDIO_OWNER');

INSERT INTO roles (id, name, created_by)
SELECT '01HZRB00000000000000000006', 'TEACHER', NULL
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'TEACHER');

-- Permissions for USER resource (generic actions)
INSERT INTO permissions (id, resource, action, scope, effect, qualifiers, created_by)
SELECT '01HZRB10000000000000000001', 'USER', 'VIEW',    'GLOBAL',  'ALLOW', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE resource='USER' AND action='VIEW' AND scope='GLOBAL');

INSERT INTO permissions (id, resource, action, scope, effect, qualifiers, created_by)
SELECT '01HZRB10000000000000000002', 'USER', 'CREATE',  'GLOBAL',  'ALLOW', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE resource='USER' AND action='CREATE' AND scope='GLOBAL');

INSERT INTO permissions (id, resource, action, scope, effect, qualifiers, created_by)
SELECT '01HZRB10000000000000000003', 'USER', 'UPDATE',  'GLOBAL',  'ALLOW', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE resource='USER' AND action='UPDATE' AND scope='GLOBAL');

INSERT INTO permissions (id, resource, action, scope, effect, qualifiers, created_by)
SELECT '01HZRB10000000000000000004', 'USER', 'DELETE',  'GLOBAL',  'ALLOW', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE resource='USER' AND action='DELETE' AND scope='GLOBAL');

-- Scoped VIEW permissions
INSERT INTO permissions (id, resource, action, scope, effect, qualifiers, created_by)
SELECT '01HZRB10000000000000000011', 'USER', 'VIEW', 'COUNTRY', 'ALLOW', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE resource='USER' AND action='VIEW' AND scope='COUNTRY');

INSERT INTO permissions (id, resource, action, scope, effect, qualifiers, created_by)
SELECT '01HZRB10000000000000000012', 'USER', 'VIEW', 'PROVINCE', 'ALLOW', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE resource='USER' AND action='VIEW' AND scope='PROVINCE');
