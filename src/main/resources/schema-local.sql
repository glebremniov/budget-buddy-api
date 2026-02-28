-- Keep existing tables for now (can be removed later if not needed)
CREATE TABLE IF NOT EXISTS users
(
    id         VARCHAR(36) PRIMARY KEY,
    version    INT                  NOT NULL,
    username   VARCHAR(255)         NOT NULL UNIQUE,
    password   VARCHAR(255)         NOT NULL,
    enabled    BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP            NOT NULL,
    updated_at TIMESTAMP            NOT NULL
);

-- Authorities/Roles table for Spring Security
CREATE TABLE IF NOT EXISTS authorities
(
    username  VARCHAR(255) NOT NULL,
    authority VARCHAR(255) NOT NULL,
    FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE,
    UNIQUE (username, authority)
);

CREATE UNIQUE INDEX ix_auth_username ON authorities (username, authority);

-- Categories table
CREATE TABLE IF NOT EXISTS categories
(
    id         VARCHAR(36) PRIMARY KEY,
    version    INT          NOT NULL,
    name       VARCHAR(255) NOT NULL,
    owner_id   VARCHAR(36)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users (id)
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions
(
    id          VARCHAR(36) PRIMARY KEY,
    version     INT         NOT NULL,
    category_id VARCHAR(36),
    amount      INT         NOT NULL,
    type        VARCHAR(50) NOT NULL,
    currency    VARCHAR(3)  NOT NULL,
    date        DATE        NOT NULL,
    description TEXT,
    created_at  TIMESTAMP   NOT NULL,
    updated_at  TIMESTAMP   NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories (id)
);
