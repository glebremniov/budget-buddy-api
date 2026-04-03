--liquibase formatted sql

--changeset g.remniov@gmail.com:002-refresh-tokens
CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY,
    token      VARCHAR(255) NOT NULL UNIQUE,
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

--rollback DROP INDEX idx_refresh_tokens_user_id;
--rollback DROP TABLE refresh_tokens;
