--liquibase formatted sql

--changeset g.remniov@gmail.com:006-hash-refresh-tokens
ALTER TABLE refresh_tokens RENAME COLUMN token TO token_hash;
ALTER TABLE refresh_tokens ALTER COLUMN token_hash TYPE CHAR(64);

--rollback ALTER TABLE refresh_tokens ALTER COLUMN token_hash TYPE VARCHAR(255);
--rollback ALTER TABLE refresh_tokens RENAME COLUMN token_hash TO token;
