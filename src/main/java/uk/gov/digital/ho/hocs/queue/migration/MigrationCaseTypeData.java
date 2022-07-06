package uk.gov.digital.ho.hocs.queue.migration;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.queue.complaints.ComplaintTypeData;

@Component
public class MigrationCaseTypeData implements ComplaintTypeData {
    @Override
    public String getCaseType() {
        return "MIGRATION";
    }

    @Override
    public String getOrigin() {
        return "MIG";
    }

}
