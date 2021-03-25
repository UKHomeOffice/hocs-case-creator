package uk.gov.digital.ho.hocs.queue.common;

import uk.gov.digital.ho.hocs.client.audit.dto.EventType;

public interface ComplaintTypeData {
    String getCaseType();
    EventType getCreateComplaintEventType();
    EventType getCreateCorrespondentEventType();
    EventType getSuccessfulValidationEvent();
    EventType getUnsuccessfulValidationEvent();
}
