--liquibase formatted sql

--changeset g.remniov@gmail.com:010-transaction-type-enum
CREATE TYPE transaction_type AS ENUM ('EXPENSE', 'INCOME');

ALTER TABLE transactions
    ALTER COLUMN type TYPE transaction_type USING type::transaction_type;

--rollback ALTER TABLE transactions ALTER COLUMN type TYPE VARCHAR(50) USING type::text::VARCHAR(50);
--rollback DROP TYPE transaction_type;
