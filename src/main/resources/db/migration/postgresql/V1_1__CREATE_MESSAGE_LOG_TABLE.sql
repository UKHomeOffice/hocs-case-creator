CREATE TABLE IF NOT EXISTS message_log
(
    message_id          VARCHAR(100)                PRIMARY KEY,
    external_reference  UUID,
    message             TEXT                        NOT NULL,
    status              VARCHAR(100),
    completed           TIMESTAMP WITH TIME ZONE,
    received            TIMESTAMP WITH TIME ZONE    NOT NULL
);
