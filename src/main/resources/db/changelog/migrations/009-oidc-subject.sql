--liquibase formatted sql

--changeset g.remniov@gmail.com:009-oidc-subject
ALTER TABLE users
    ADD COLUMN oidc_issuer VARCHAR(255) NOT NULL DEFAULT '${oidc-issuer-uri}';

ALTER TABLE users
    ALTER COLUMN oidc_issuer DROP DEFAULT;

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_oidc_subject_key;

ALTER TABLE users
    ADD CONSTRAINT users_oidc_subject_oidc_issuer_key UNIQUE (oidc_subject, oidc_issuer);

--rollback ALTER TABLE users DROP CONSTRAINT IF EXISTS users_oidc_subject_oidc_issuer_key;
--rollback ALTER TABLE users ADD CONSTRAINT users_oidc_subject_key UNIQUE (oidc_subject);
--rollback ALTER TABLE users ALTER COLUMN oidc_issuer DROP DEFAULT;
--rollback ALTER TABLE users DROP COLUMN oidc_issuer;
