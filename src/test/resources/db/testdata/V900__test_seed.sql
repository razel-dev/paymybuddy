-- src/test/resources/db/testdata/V900__test_seed.sql
-- Création des utilisateurs nécessaires aux FKs des comptes
INSERT INTO users (id, username, email, password)
VALUES (100, 'alice', 'alice@test.local', 'hash');

INSERT INTO users (id, username, email, password)
VALUES (101, 'bob', 'bob@test.local', 'hash');

