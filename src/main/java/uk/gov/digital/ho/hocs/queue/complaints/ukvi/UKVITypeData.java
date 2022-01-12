package uk.gov.digital.ho.hocs.queue.complaints.ukvi;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.queue.complaints.ComplaintTypeData;

@Component
public class UKVITypeData implements ComplaintTypeData {
    @Override
    public String getCaseType() {
        return "COMP";
    }

    @Override
    public String getOrigin() {
        return "Webform";
    }

}
