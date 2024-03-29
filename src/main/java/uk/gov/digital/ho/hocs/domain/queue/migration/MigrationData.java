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
    public static final String CREATION_DATE = "$.creationDate";
    public static final String RECEIVED_DATE = "$.dateReceived";
    public static final String MIGRATED_REFERENCE = "$.sourceCaseId";
    public static final String CASE_DATA = "$.caseData";
    public static final String CASE_DEADLINE = "$.deadlineDate";
    public static final String PRIMARY_TOPIC = "$.primaryTopic";

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

    public String getCaseDataJson() {
        return ctx.read(CASE_DATA, JSONArray.class).toJSONString();
    }

    public LocalDate getDateCreated() { return LocalDate.parse(ctx.read(CREATION_DATE)); }

    @Override
    public LocalDate getDateReceived() {
        return LocalDate.parse(ctx.read(RECEIVED_DATE));
    }

    public LocalDate getCaseDeadline() {
        try {
            CharSequence read = ctx.read(CASE_DEADLINE);
            return LocalDate.parse(read);
        }
        catch (Exception e) {
            return null;
        }
    }

    public LinkedHashMap<String, Object> getPrimaryCorrespondent() {return ctx.read(PRIMARY_CORRESPONDENT);}

    public Optional<String> getAdditionalCorrespondents() {
        return optionalString(ctx, ADDITIONAL_CORRESPONDENTS);
    }

    public String getMigratedReference() {
        return ctx.read(MIGRATED_REFERENCE);
    }

    public Optional<String> getPrimaryTopic() {
        try {
            return Optional.ofNullable(ctx.read(PRIMARY_TOPIC));
        } catch (PathNotFoundException e) {
            return Optional.empty();
        }
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
