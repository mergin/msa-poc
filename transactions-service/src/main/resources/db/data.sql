-- Seed 1 000 transactions spread across accounts a-001..a-100 (10 per account).
-- Timestamps span the last 12 months to enable meaningful monthly summaries.
-- Amounts are varied so that the anomaly-detection query (mean + 2σ) finds outliers.
INSERT INTO transactions (id, account_id, type, amount, currency, description, timestamp, category)
SELECT
  'tx-' || LPAD(gs::text, 5, '0')                         AS id,
  'a-'  || LPAD(((gs - 1) / 10 + 1)::text, 3, '0')       AS account_id,
  CASE WHEN gs % 3 = 0 THEN 'CREDIT' ELSE 'DEBIT' END     AS type,
  -- Mix of small routine amounts and occasional large outliers (every 47th row)
  CASE
    WHEN gs % 47 = 0 THEN ROUND((5000 + gs * 1.37)::numeric, 2)
    ELSE             ROUND((20  + (gs % 200) * 2.15)::numeric, 2)
  END                                                      AS amount,
  'EUR'                                                    AS currency,
  CASE WHEN gs % 3 = 0 THEN 'credit #' ELSE 'debit #' END
    || gs                                                  AS description,
  -- Spread over the last 365 days, newer rows for lower gs values
  NOW() - (gs || ' hours')::interval                      AS timestamp,
  CASE (gs % 6)
    WHEN 0 THEN 'GROCERIES'
    WHEN 1 THEN 'UTILITIES'
    WHEN 2 THEN 'SALARY'
    WHEN 3 THEN 'RENT'
    WHEN 4 THEN 'ENTERTAINMENT'
    ELSE        'TRANSFER'
  END                                                      AS category
FROM generate_series(1, 1000) AS gs
ON CONFLICT (id) DO NOTHING;