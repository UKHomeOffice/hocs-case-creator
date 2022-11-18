package uk.gov.digital.ho.hocs.queue.migration;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.queue.data.CaseData;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
public class MigrationData extends CaseData {

    private static final String COMPLAINT_TYPE = "$.caseType";
    private static final String PRIMARY_CORRESPONDENT = "$.primaryCorrespondent";
    private static final String ADDITIONAL_CORRESPONDENTS = "$.additionalCorrespondents";

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

    public LinkedHashMap getPrimaryCorrespondent() { return ctx.read(PRIMARY_CORRESPONDENT); }

    public JSONArray getAdditionalCorrespondents() { return ctx.read(ADDITIONAL_CORRESPONDENTS); }
}
