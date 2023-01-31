DROP TABLE IF EXISTS message_log cascade;

CREATE TABLE IF NOT EXISTS message_log
(
    uuid                UUID                PRIMARY KEY,
    message_id          VARCHAR(100),
    external_reference  UUID,
    message             TEXT                NOT NULL,
    status              VARCHAR(100),
    completed           TIMESTAMP,
    received            TIMESTAMP           NOT NULL
);