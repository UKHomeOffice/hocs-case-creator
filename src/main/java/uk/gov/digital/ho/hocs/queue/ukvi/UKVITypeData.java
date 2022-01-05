package uk.gov.digital.ho.hocs.queue.ukvi;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.queue.common.ComplaintTypeData;

@Component
public class UKVITypeData implements ComplaintTypeData {
    @Override
    public String getCaseType() {
        return "COMP";
    }
}
