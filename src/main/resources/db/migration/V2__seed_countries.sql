-- Seed minimal countries for name resolution (idempotent without relying on unique constraints)
INSERT INTO country (id, code2, code3, name)
SELECT '01HZY0AK000000000000000000', 'SG', 'SGP', 'singapore'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code2 = 'SG');

INSERT INTO country (id, code2, code3, name)
SELECT '01HZY0AM000000000000000000', 'MY', 'MYS', 'malaysia'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code2 = 'MY');
