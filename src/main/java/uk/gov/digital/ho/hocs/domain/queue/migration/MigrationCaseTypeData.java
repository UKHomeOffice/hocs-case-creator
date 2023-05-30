package uk.gov.digital.ho.hocs.domain.queue.migration;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.domain.queue.complaints.ComplaintTypeData;

@Component
@NoArgsConstructor
public class MigrationCaseTypeData implements ComplaintTypeData {

    private String caseType = null;

    public MigrationCaseTypeData(String caseType) {
        this.caseType = caseType;
    }

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
