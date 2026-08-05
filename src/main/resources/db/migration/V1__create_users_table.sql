-- Flyway runs files in this folder in order, tracking which ones have
-- already been applied in a table it creates for itself (flyway_schema_history).
-- Naming convention: V<version>__<description>.sql — the double underscore matters.

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
