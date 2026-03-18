--liquibase formatted sql

--changeset g.remniov@gmail.com:006-transaction-category-not-null
ALTER TABLE transactions
    ALTER COLUMN category_id SET NOT NULL;

--rollback ALTER TABLE transactions ALTER COLUMN category_id DROP NOT NULL;
