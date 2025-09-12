-- V7__rbac_assignments.sql
-- Assign HQ and SUPER_ADMIN roles to global USER permissions

-- Map role -> permission (USER GLOBAL VIEW/CREATE/UPDATE/DELETE)
-- SUPER_ADMIN
INSERT INTO role_permissions (id, role_id, permission_id, created_by)
SELECT '01HZRB20000000000000000001', r.id, p.id, NULL
FROM roles r, permissions p
WHERE r.name = 'SUPER_ADMIN' AND p.resource='USER' AND p.scope='GLOBAL' AND p.action='VIEW'
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);

INSERT INTO role_permissions (id, role_id, permission_id, created_by)
SELECT '01HZRB20000000000000000002', r.id, p.id, NULL
FROM roles r, permissions p
WHERE r.name = 'SUPER_ADMIN' AND p.resource='USER' AND p.scope='GLOBAL' AND p.action='CREATE'
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);

INSERT INTO role_permissions (id, role_id, permission_id, created_by)
SELECT '01HZRB20000000000000000003', r.id, p.id, NULL
FROM roles r, permissions p
WHERE r.name = 'SUPER_ADMIN' AND p.resource='USER' AND p.scope='GLOBAL' AND p.action='UPDATE'
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);

INSERT INTO role_permissions (id, role_id, permission_id, created_by)
SELECT '01HZRB20000000000000000004', r.id, p.id, NULL
FROM roles r, permissions p
WHERE r.name = 'SUPER_ADMIN' AND p.resource='USER' AND p.scope='GLOBAL' AND p.action='DELETE'
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);

-- HQ
INSERT INTO role_permissions (id, role_id, permission_id, created_by)
SELECT '01HZRB20000000000000000011', r.id, p.id, NULL
FROM roles r, permissions p
WHERE r.name = 'HQ' AND p.resource='USER' AND p.scope='GLOBAL' AND p.action='VIEW'
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);

INSERT INTO role_permissions (id, role_id, permission_id, created_by)
SELECT '01HZRB20000000000000000012', r.id, p.id, NULL
FROM roles r, permissions p
WHERE r.name = 'HQ' AND p.resource='USER' AND p.scope='GLOBAL' AND p.action='CREATE'
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);

INSERT INTO role_permissions (id, role_id, permission_id, created_by)
SELECT '01HZRB20000000000000000013', r.id, p.id, NULL
FROM roles r, permissions p
WHERE r.name = 'HQ' AND p.resource='USER' AND p.scope='GLOBAL' AND p.action='UPDATE'
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);

INSERT INTO role_permissions (id, role_id, permission_id, created_by)
SELECT '01HZRB20000000000000000014', r.id, p.id, NULL
FROM roles r, permissions p
WHERE r.name = 'HQ' AND p.resource='USER' AND p.scope='GLOBAL' AND p.action='DELETE'
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
