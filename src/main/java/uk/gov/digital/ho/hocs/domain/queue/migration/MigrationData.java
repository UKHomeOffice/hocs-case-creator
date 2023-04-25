package uk.gov.digital.ho.hocs.domain.queue.migration;

import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.domain.queue.data.CaseData;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MigrationData extends CaseData {

    private static final String COMPLAINT_TYPE = "$.caseType";
    private static final String PRIMARY_CORRESPONDENT = "$.primaryCorrespondent";
    private static final String CASE_STATUS = "$.caseStatus";
    private static final String CASE_STATUS_DATE = "$.caseStatusDate";
    private static final String ADDITIONAL_CORRESPONDENTS = "$.additionalCorrespondents";
    private static final String CASE_ATTACHMENTS = "$.caseAttachments";
    public static final String CLOSED_STATUS = "closed";

    public MigrationData(String jsonBody) {
        super(jsonBody);
    }

    @Override
    public String getComplaintType() {
        return ctx.read(COMPLAINT_TYPE);
    }

    public String getCaseStatus() {
        return ctx.read(CASE_STATUS);
    }

    public LocalDate getCaseStatusDate() {
        return LocalDate.parse(ctx.read(CASE_STATUS_DATE));
    }

    public String getCaseAttachments() {
        return ctx.read(CASE_ATTACHMENTS).toString();
    }

    @Override
    public List<ComplaintCorrespondent> getComplaintCorrespondent() {
        return null;
    }

    public LocalDate getDateCompleted() {
        return getCaseStatus().equalsIgnoreCase(CLOSED_STATUS)
            ? getCaseStatusDate()
            : null;
    }

    public LinkedHashMap getPrimaryCorrespondent() {return ctx.read(PRIMARY_CORRESPONDENT);}

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
