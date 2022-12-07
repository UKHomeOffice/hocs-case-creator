package uk.gov.digital.ho.hocs.application;

public enum LogEvent {
    REST_HELPER_POST,
    REST_HELPER_PUT,
    REST_HELPER_GET,
    AUDIT_EVENT_CREATED,
    AUDIT_FAILED,
    CONFIG_PARSE_FAILURE;

    public static final String EVENT = "event_id";
    public static final String EXCEPTION = "exception";
}
