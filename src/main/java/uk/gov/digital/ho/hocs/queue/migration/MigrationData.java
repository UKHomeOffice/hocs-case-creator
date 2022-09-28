package uk.gov.digital.ho.hocs.queue.migration;

import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.queue.complaints.CorrespondentType;
import uk.gov.digital.ho.hocs.queue.data.CaseData;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MigrationData extends CaseData {

    private static final String COMPLAINT_TYPE = "$.caseType";
    private static final String COMPLAINT_CORRESPONDENT_FULLNAME = "$.correspondentName";

    public MigrationData(String jsonBody) {
        super(jsonBody);
    }

    @Override
    public String getComplaintType() {
        return ctx.read(COMPLAINT_TYPE);
    }

    /**
     *  This assumes only one correspondent per migration message. To verify once we received the data.
     *  @return a list of correspondent objects
     */
    @Override
    public List<ComplaintCorrespondent> getComplaintCorrespondent() {
        List<ComplaintCorrespondent> correspondents = new ArrayList<>();
        correspondents.add(new ComplaintCorrespondent(ctx.read(COMPLAINT_CORRESPONDENT_FULLNAME), CorrespondentType.COMPLAINANT));
        return correspondents;
    }

}
