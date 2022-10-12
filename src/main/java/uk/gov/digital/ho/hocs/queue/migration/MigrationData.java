package uk.gov.digital.ho.hocs.queue.migration;

import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.workflow.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.queue.data.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MigrationData extends CaseData {

    private static final String COMPLAINT_TYPE = "$.caseType";
    private static final String CASE_ATTACHMENTS = "$.caseAttachments";

    public MigrationData(String jsonBody) {
        super(jsonBody);
    }

    @Override
    public String getComplaintType() {
        return ctx.read(COMPLAINT_TYPE);
    }

    /**
     * Returns an empty list for now. Will add the logic once we received the data.
     * @return
     */
    @Override
    public List<ComplaintCorrespondent> getComplaintCorrespondent() {
        List<ComplaintCorrespondent> correspondents = new ArrayList<>();
        return correspondents;
    }

    public String getCaseAttachments() {
       return ctx.read(CASE_ATTACHMENTS).toString();
    }


}
