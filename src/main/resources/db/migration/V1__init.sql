CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE accounts (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          user_id INT NOT NULL,
                          account_name VARCHAR(100) NOT NULL,
                          currency CHAR(3) NOT NULL DEFAULT 'EUR',
                          balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE transactions (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              sender_account_id INT NOT NULL,
                              receiver_account_id INT NOT NULL,
                              description VARCHAR(255),
                              amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
                              fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (sender_account_id) REFERENCES accounts(id),
                              FOREIGN KEY (receiver_account_id) REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bank_transfers (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                account_id INT NOT NULL,
                                amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
                                type ENUM('DEPOSIT','WITHDRAWAL') NOT NULL,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (account_id) REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_connections (
                                  owner_user_id INT NOT NULL,
                                  related_user_id INT NOT NULL,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (owner_user_id, related_user_id),
                                  FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE,
                                  FOREIGN KEY (related_user_id) REFERENCES users(id) ON DELETE CASCADE,
                                  CHECK (owner_user_id <> related_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes
CREATE INDEX ix_accounts_user       ON accounts(user_id);
CREATE INDEX ix_tx_sender_created   ON transactions(sender_account_id, created_at);
CREATE INDEX ix_tx_receiver_created ON transactions(receiver_account_id, created_at);
CREATE INDEX ix_uc_owner_created    ON user_connections(owner_user_id, created_at);