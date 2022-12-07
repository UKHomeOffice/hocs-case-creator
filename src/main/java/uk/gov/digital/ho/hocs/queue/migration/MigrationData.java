package uk.gov.digital.ho.hocs.queue.migration;

import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.queue.data.CaseData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MigrationData extends CaseData {

    private static final String COMPLAINT_TYPE = "$.caseType";
    private static final String PRIMARY_CORRESPONDENT = "$.primaryCorrespondent";
    private static final String ADDITIONAL_CORRESPONDENTS = "$.additionalCorrespondents";
    private static final String CASE_ATTACHMENTS = "$.caseAttachments";

    public MigrationData(String jsonBody) {
        super(jsonBody);
    }

    @Override
    public String getComplaintType() {
        return ctx.read(COMPLAINT_TYPE);
    }

    public String getCaseAttachments() {
        return ctx.read(CASE_ATTACHMENTS).toString();
    }

    @Override
    public List<ComplaintCorrespondent> getComplaintCorrespondent() {
        return null;
    }

    public LinkedHashMap getPrimaryCorrespondent() { return ctx.read(PRIMARY_CORRESPONDENT); }

    public Optional<String> getAdditionalCorrespondents() {
        return optionalString(ctx, ADDITIONAL_CORRESPONDENTS);
    }

    public Optional<String> optionalString(ReadContext ctx, String path) {
        try {
            JSONArray value = ctx.read(path);
            return Optional.of(value.toJSONString());
        } catch (PathNotFoundException e) {
            return Optional.empty();
        }
    }
}
