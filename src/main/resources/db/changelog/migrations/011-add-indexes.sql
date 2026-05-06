--liquibase formatted sql

--changeset g.remniov@gmail.com:011-add-indexes
CREATE INDEX idx_transactions_owner_date ON transactions (owner_id, date DESC, created_at DESC);
CREATE INDEX idx_transactions_owner_category ON transactions (owner_id, category_id);
CREATE INDEX idx_categories_owner_id ON categories (owner_id);

--rollback DROP INDEX idx_categories_owner_id;
--rollback DROP INDEX idx_transactions_owner_category;
--rollback DROP INDEX idx_transactions_owner_date;
