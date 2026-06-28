-- Placeholder migration proving the Flyway pipeline works end to end.
-- Safe noop: creates a harmless metadata table. Replace/augment with real
-- schema migrations as the application grows.
CREATE TABLE IF NOT EXISTS app_meta (
    id        BIGSERIAL PRIMARY KEY,
    key       VARCHAR(100) NOT NULL UNIQUE,
    value     VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);