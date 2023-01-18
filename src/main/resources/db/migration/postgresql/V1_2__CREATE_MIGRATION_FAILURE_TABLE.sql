DROP TABLE IF EXISTS migration_failures cascade;

CREATE TABLE IF NOT EXISTS migration_failures
(
    cms_id                      NUMERIC,
    failure_reason              TEXT
);