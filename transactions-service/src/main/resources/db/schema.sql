CREATE TABLE IF NOT EXISTS transactions (
  id          VARCHAR(50)    PRIMARY KEY,
  account_id  VARCHAR(50)    NOT NULL,
  type        VARCHAR(10)    NOT NULL,
  amount      NUMERIC(19, 2) NOT NULL,
  currency    VARCHAR(10)    NOT NULL,
  description VARCHAR(255)   NOT NULL,
  timestamp   TIMESTAMP      NOT NULL,
  category    VARCHAR(50)    NOT NULL
);

-- Indexes to support the analytical queries efficiently
CREATE INDEX IF NOT EXISTS idx_transactions_account_id
  ON transactions (account_id);

CREATE INDEX IF NOT EXISTS idx_transactions_account_timestamp
  ON transactions (account_id, timestamp);

CREATE INDEX IF NOT EXISTS idx_transactions_category
  ON transactions (category);

CREATE INDEX IF NOT EXISTS idx_transactions_timestamp
  ON transactions (timestamp DESC);