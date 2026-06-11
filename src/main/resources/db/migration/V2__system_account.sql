INSERT INTO users (username, email, password)
SELECT 'system-paymybuddy', 'system@paymybuddy.local', 'SYSTEM_ACCOUNT_DISABLED'
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'system@paymybuddy.local'
);

INSERT INTO accounts (user_id, account_name, currency, balance)
SELECT u.id, 'PayMyBuddy Fees', 'EUR', 0.00
FROM users u
WHERE u.email = 'system@paymybuddy.local'
  AND NOT EXISTS (
      SELECT 1
      FROM accounts a
      WHERE a.user_id = u.id
        AND a.account_name = 'PayMyBuddy Fees'
  );
