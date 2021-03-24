package uk.gov.digital.ho.hocs.queue.ukvi;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.client.audit.dto.EventType;
import uk.gov.digital.ho.hocs.queue.common.ComplaintTypeData;

@Component
public class UKVITypeData implements ComplaintTypeData {
    @Override
    public String getCaseType() {
        return "COMP";
    }

    @Override
    public EventType getCreateComplaintEventType() {
        return EventType.UKVI_COMPLAINT_CREATED;
    }

    @Override
    public EventType getCreateCorrespondentEventType() {
        return EventType.UKVI_CORRESPONDENT_CREATED;
    }
}
