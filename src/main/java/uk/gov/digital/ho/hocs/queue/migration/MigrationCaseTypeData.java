package uk.gov.digital.ho.hocs.queue.migration;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.queue.complaints.ComplaintTypeData;

@Component
public class MigrationCaseTypeData implements ComplaintTypeData {

    private String caseType = null;

    @Override
    public String getCaseType() {
        return caseType;
    }

    @Override
    public String getOrigin() {
        return "CMS";
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }
}
