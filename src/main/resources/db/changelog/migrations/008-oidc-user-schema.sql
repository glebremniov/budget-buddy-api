--liquibase formatted sql

--changeset g.remniov@gmail.com:008-oidc-user-schema

-- Drop auth tables no longer needed (authentication moved to external OIDC provider)
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS authorities;

-- Wipe existing users — they must re-register via the OIDC provider.
-- Domain tables have non-cascading FKs to users(id), so delete them first.
DELETE FROM transactions;
DELETE FROM categories;
DELETE FROM users;

-- Remove columns managed by OIDC provider
ALTER TABLE users DROP COLUMN password;
ALTER TABLE users
    DROP COLUMN username;
ALTER TABLE users
    DROP COLUMN enabled;

-- Add OIDC subject column (maps JWT sub claim to local user)
ALTER TABLE users ADD COLUMN oidc_subject VARCHAR(255) NOT NULL UNIQUE;

--rollback ALTER TABLE users DROP COLUMN oidc_subject;
--rollback ALTER TABLE users ADD COLUMN enabled BOOLEAN DEFAULT TRUE NOT NULL;
--rollback ALTER TABLE users ADD COLUMN username VARCHAR(255) NOT NULL UNIQUE DEFAULT '';
--rollback ALTER TABLE users ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '';
--rollback CREATE TABLE authorities (username VARCHAR(255) NOT NULL, authority VARCHAR(255) NOT NULL, FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE, UNIQUE (username, authority));
--rollback CREATE TABLE refresh_tokens (id UUID PRIMARY KEY, token_hash CHAR(64) NOT NULL UNIQUE, user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE, expires_at TIMESTAMPTZ NOT NULL, created_at TIMESTAMPTZ NOT NULL);
--rollback CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
