package uk.gov.digital.ho.hocs.queue.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.MigrationComplaintCorrespondent;
import uk.gov.digital.ho.hocs.queue.complaints.CorrespondentType;
import uk.gov.digital.ho.hocs.queue.data.CaseData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class MigrationData extends CaseData {

    private static final String COMPLAINT_TYPE = "$.caseType";
    private static final String PRIMARY_CORRESPONDENT = "$.primaryCorrespondent";

    public MigrationData(String jsonBody) {
        super(jsonBody);
    }

    @Override
    public String getComplaintType() {
        return ctx.read(COMPLAINT_TYPE);
    }

    @Override
    public List<ComplaintCorrespondent> getComplaintCorrespondent() {
        return null;
    }

    /**
     *  This assumes only one correspondent per migration message. To verify once we received the data.
     *  @return a list of correspondent objects
     */

    public String getPrimaryCorrespondent() {
        return ctx.read(PRIMARY_CORRESPONDENT).toString();
    }

}
