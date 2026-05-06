--liquibase formatted sql

--changeset g.remniov@gmail.com:012-cascade-user-delete
ALTER TABLE categories
    DROP CONSTRAINT categories_owner_id_fkey,
    ADD CONSTRAINT categories_owner_id_fkey
        FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE transactions
    DROP CONSTRAINT transactions_owner_id_fkey,
    ADD CONSTRAINT transactions_owner_id_fkey
        FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE;

--rollback ALTER TABLE transactions DROP CONSTRAINT transactions_owner_id_fkey;
--rollback ALTER TABLE transactions ADD CONSTRAINT transactions_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES users (id);
--rollback ALTER TABLE categories DROP CONSTRAINT categories_owner_id_fkey;
--rollback ALTER TABLE categories ADD CONSTRAINT categories_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES users (id);
