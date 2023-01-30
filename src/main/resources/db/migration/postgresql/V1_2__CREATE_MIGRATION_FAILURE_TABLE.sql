DROP TABLE IF EXISTS migration_failures cascade;

CREATE TABLE IF NOT EXISTS migration_failures
(
    external_reference          UUID,
    failure_reason              TEXT
);