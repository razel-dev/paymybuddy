CREATE TABLE ledger_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    transaction_id INT NULL,
    bank_transfer_id INT NULL,
    entry_type ENUM('TRANSFER','BANK_TRANSFER','FEE') NOT NULL,
    side ENUM('DEBIT','CREDIT') NOT NULL,
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL DEFAULT 'EUR',
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    FOREIGN KEY (bank_transfer_id) REFERENCES bank_transfers(id),
    CHECK (
        (transaction_id IS NOT NULL AND bank_transfer_id IS NULL)
        OR (transaction_id IS NULL AND bank_transfer_id IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_ledger_account_created ON ledger_entries(account_id, created_at);
CREATE INDEX ix_ledger_tx ON ledger_entries(transaction_id);
CREATE INDEX ix_ledger_bank_transfer ON ledger_entries(bank_transfer_id);
