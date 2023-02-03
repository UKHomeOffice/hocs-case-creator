CREATE TABLE IF NOT EXISTS message_log
(
    message_id          VARCHAR(100)                PRIMARY KEY,
    external_reference  UUID,
    case_uuid           UUID,
    message             TEXT                        NOT NULL,
    status              VARCHAR(100)                NOT NULL,
    completed           TIMESTAMP WITH TIME ZONE,
    received            TIMESTAMP WITH TIME ZONE    NOT NULL
);

CREATE INDEX IF NOT EXISTS message_log_external_reference_idx ON message_log (external_reference) WHERE external_reference IS NOT NULL;
CREATE INDEX IF NOT EXISTS message_log_case_uuid_idx ON message_log (case_uuid) WHERE case_uuid IS NOT NULL;
CREATE INDEX IF NOT EXISTS message_log_status_idx ON message_log (status);
