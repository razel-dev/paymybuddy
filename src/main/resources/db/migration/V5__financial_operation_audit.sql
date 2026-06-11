CREATE TABLE financial_operation_audit (
    id INT AUTO_INCREMENT PRIMARY KEY,
    operation_type VARCHAR(32) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    source_id INT NOT NULL,
    actor_user_id INT NOT NULL,
    account_id INT NOT NULL,
    counterparty_account_id INT NULL,
    amount DECIMAL(10,2) NOT NULL,
    fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    currency CHAR(3) NOT NULL,
    description VARCHAR(255) NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (actor_user_id) REFERENCES users(id),
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    FOREIGN KEY (counterparty_account_id) REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_audit_actor_occurred_at ON financial_operation_audit(actor_user_id, occurred_at);
CREATE INDEX ix_audit_account_occurred_at ON financial_operation_audit(account_id, occurred_at);
CREATE INDEX ix_audit_source ON financial_operation_audit(source_type, source_id);
