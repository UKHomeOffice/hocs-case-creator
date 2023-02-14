package uk.gov.digital.ho.hocs.application;

public enum LogEvent {
    AUDIT_EVENT_CREATED,
    AUDIT_FAILED,
    CASE_CORRESPONDENTS_FAILURE,
    CASE_CREATION_FAILURE,
    CASE_DOCUMENT_CREATION_FAILURE,
    CASE_MIGRATION_FAILURE,
    CASE_STAGE_RETRIEVAL_FAILURE,
    CASE_TEAM_UPDATE_FAILURE,
    CASE_USER_UPDATE_FAILURE,
    CONFIG_PARSE_FAILURE,
    REST_HELPER_GET,
    REST_HELPER_POST,
    REST_HELPER_PUT;

    public static final String EVENT = "event_id";
    public static final String EXCEPTION = "exception";
}
