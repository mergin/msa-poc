INSERT INTO accounts (id, account_number, type, balance, currency, owner_id)
SELECT
  'a-' || LPAD(gs::text, 3, '0') AS id,
  'ES12-0049-' || LPAD(gs::text, 4, '0') AS account_number,
  CASE
    WHEN gs % 3 = 0 THEN 'CHECKING'
    WHEN gs % 3 = 1 THEN 'SAVINGS'
    ELSE 'CREDIT'
  END AS type,
  ROUND((100 + gs * 37.15)::numeric, 2) AS balance,
  'EUR' AS currency,
  'c-' || LPAD(gs::text, 3, '0') AS owner_id
FROM generate_series(1, 100) AS gs
ON CONFLICT (id) DO NOTHING;
