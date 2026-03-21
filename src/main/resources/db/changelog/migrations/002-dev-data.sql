--liquibase formatted sql

--changeset g.remniov@gmail.com:002-dev-data context:dev
INSERT INTO users (id, version, username, password, enabled, created_at, updated_at)
VALUES ('a788506a-45e8-4e87-92e9-974bd5863311',
        1,
        'admin',
        '{noop}8a98232f-76f4-4819-b868-91682b52ad3b',
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO authorities (username, authority)
VALUES ('admin', 'ROLE_ADMIN')
ON CONFLICT (username, authority) DO NOTHING;

INSERT INTO categories (id, version, name, owner_id, created_at, updated_at)
VALUES ('5910a910-4ed1-47e8-b0e5-73b27f0d5add',
        1,
        'Food',
        'a788506a-45e8-4e87-92e9-974bd5863311',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

--rollback DELETE FROM categories WHERE id = '5910a910-4ed1-47e8-b0e5-73b27f0d5add';
--rollback DELETE FROM authorities WHERE username = 'admin';
--rollback DELETE FROM users WHERE id = 'a788506a-45e8-4e87-92e9-974bd5863311';
