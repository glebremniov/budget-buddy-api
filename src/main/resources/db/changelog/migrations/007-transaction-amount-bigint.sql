--liquibase formatted sql

--changeset g.remniov@gmail.com:007-transaction-amount-bigint
ALTER TABLE transactions
    ALTER COLUMN amount TYPE BIGINT;

--rollback ALTER TABLE transactions ALTER COLUMN amount TYPE INT;
