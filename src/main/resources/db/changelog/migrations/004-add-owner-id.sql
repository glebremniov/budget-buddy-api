--liquibase formatted sql

--changeset g.remniov@gmail.com:004-add-owner-id
ALTER TABLE transactions
    ADD COLUMN owner_id UUID NOT NULL REFERENCES users (id);

--rollback ALTER TABLE transactions DROP COLUMN owner_id;
