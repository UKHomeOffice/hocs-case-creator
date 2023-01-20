DROP TABLE IF EXISTS migration_state cascade;

CREATE TABLE IF NOT EXISTS migration_state
(
    case_uuid                  UUID,
    stage_uuid                 UUID,
    cms_id                     NUMERIC,
    message_body               TEXT,
    current_state              TEXT,
    in_progress                BOOLEAN
);