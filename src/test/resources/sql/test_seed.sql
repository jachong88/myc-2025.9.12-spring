-- Test seed for default current user and HQ role assignment
INSERT INTO users (id, email, full_name, is_active)
SELECT '01ABCDEFGHJKMNPQRSTVWXYZ12', 'creator@example.com', 'Test Creator', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE LOWER(email) = LOWER('creator@example.com'));

INSERT INTO user_roles (id, user_id, role_id, created_by)
SELECT '01HZRBUR000000000000000001', u.id, r.id, NULL
FROM users u, roles r
WHERE u.email = 'creator@example.com' AND r.name = 'HQ'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
