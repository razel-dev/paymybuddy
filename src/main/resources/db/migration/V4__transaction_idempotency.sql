ALTER TABLE transactions
ADD COLUMN idempotency_key VARCHAR(64) NULL;

UPDATE transactions
SET idempotency_key = CONCAT('legacy-', id)
WHERE idempotency_key IS NULL;

ALTER TABLE transactions
MODIFY COLUMN idempotency_key VARCHAR(64) NOT NULL;

ALTER TABLE transactions
ADD CONSTRAINT uk_transactions_idempotency_key UNIQUE (idempotency_key);
