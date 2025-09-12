-- Seed provinces/states for Singapore (SG) and Malaysia (MY)
-- Idempotent inserts using WHERE NOT EXISTS

-- Singapore regions
INSERT INTO province (id, country_id, code, name)
SELECT '01HZY0SG000000000000000001', c.id, 'SG-01', 'Central Singapore'
FROM country c WHERE c.code2 = 'SG'
  AND NOT EXISTS (SELECT 1 FROM province p WHERE p.code = 'SG-01');

INSERT INTO province (id, country_id, code, name)
SELECT '01HZY0SG000000000000000002', c.id, 'SG-02', 'North East'
FROM country c WHERE c.code2 = 'SG'
  AND NOT EXISTS (SELECT 1 FROM province p WHERE p.code = 'SG-02');

INSERT INTO province (id, country_id, code, name)
SELECT '01HZY0SG000000000000000003', c.id, 'SG-03', 'North West'
FROM country c WHERE c.code2 = 'SG'
  AND NOT EXISTS (SELECT 1 FROM province p WHERE p.code = 'SG-03');

INSERT INTO province (id, country_id, code, name)
SELECT '01HZY0SG000000000000000004', c.id, 'SG-04', 'South East'
FROM country c WHERE c.code2 = 'SG'
  AND NOT EXISTS (SELECT 1 FROM province p WHERE p.code = 'SG-04');

INSERT INTO province (id, country_id, code, name)
SELECT '01HZY0SG000000000000000005', c.id, 'SG-05', 'South West'
FROM country c WHERE c.code2 = 'SG'
  AND NOT EXISTS (SELECT 1 FROM province p WHERE p.code = 'SG-05');

-- Malaysia examples
INSERT INTO province (id, country_id, code, name)
SELECT '01HZY0MY000000000000000001', c.id, 'MY-01', 'Johor'
FROM country c WHERE c.code2 = 'MY'
  AND NOT EXISTS (SELECT 1 FROM province p WHERE p.code = 'MY-01');

INSERT INTO province (id, country_id, code, name)
SELECT '01HZY0MY000000000000000010', c.id, 'MY-10', 'Selangor'
FROM country c WHERE c.code2 = 'MY'
  AND NOT EXISTS (SELECT 1 FROM province p WHERE p.code = 'MY-10');

INSERT INTO province (id, country_id, code, name)
SELECT '01HZY0MY000000000000000014', c.id, 'MY-14', 'Kuala Lumpur'
FROM country c WHERE c.code2 = 'MY'
  AND NOT EXISTS (SELECT 1 FROM province p WHERE p.code = 'MY-14');
