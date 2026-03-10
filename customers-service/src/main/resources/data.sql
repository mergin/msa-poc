INSERT INTO customers (id, name, email, status)
SELECT
  'c-' || LPAD(gs::text, 3, '0') AS id,
  'Customer ' || gs AS name,
  'customer' || gs || '@example.com' AS email,
  CASE WHEN gs % 2 = 0 THEN 'ACTIVE' ELSE 'INACTIVE' END AS status
FROM generate_series(1, 100) AS gs
ON CONFLICT (id) DO NOTHING;
