package uk.gov.digital.ho.hocs.queue.data;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.document.JSONToSimpleTextConverter;
import uk.gov.digital.ho.hocs.queue.complaints.ComplaintData;
import uk.gov.digital.ho.hocs.queue.complaints.CorrespondentType;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class CaseData implements ComplaintData {

    static final String CREATION_DATE = "$.creationDate";
    static final String COMPLAINT_TYPE = "$.complaint.complaintType";
    static final String APPLICANT_TYPE = "$.complaint.reporterDetails.applicantType";
    static final String APPLICANT_APPLICANT_NAME = "$.complaint.reporterDetails.applicantName";
    static final String APPLICANT_APPLICANT_EMAIL = "$.complaint.reporterDetails.applicantEmail";
    static final String APPLICANT_APPLICANT_PHONE = "$.complaint.reporterDetails.applicantPhone";
    static final String AGENT_APPLICANT_NAME = "$.complaint.reporterDetails.applicantDetails.applicantName";
    static final String AGENT_AGENT_NAME = "$.complaint.reporterDetails.agentDetails.agentName";
    static final String AGENT_AGENT_EMAIL = "$.complaint.reporterDetails.agentDetails.agentEmail";
    static final String AGENT_AGENT_PHONE = "$.complaint.reporterDetails.agentDetails.agentPhone";

    private final ReadContext ctx;
    private final String jsonBody;

    public CaseData(String jsonBody) {
        this.jsonBody = jsonBody;
        ctx = JsonPath.parse(jsonBody);
    }

    @Override
    public LocalDate getDateReceived() {
        return LocalDate.parse(ctx.read(CREATION_DATE));
    }

    @Override
    public String getComplaintType() {
        return ctx.read(COMPLAINT_TYPE);
    }

    @Override
    public List<ComplaintCorrespondent> getComplaintCorrespondent() {

        List<ComplaintCorrespondent> correspondents = new ArrayList<>();

        try {
            String applicantType = ctx.read(APPLICANT_TYPE);
            if (applicantType.equals("APPLICANT")) {
                ComplaintCorrespondent applicantCorrespondent = new ComplaintCorrespondent(ctx.read(APPLICANT_APPLICANT_NAME), CorrespondentType.COMPLAINANT);
                optionalString(ctx, APPLICANT_APPLICANT_EMAIL).ifPresent(applicantCorrespondent::setEmail);
                optionalString(ctx, APPLICANT_APPLICANT_PHONE).ifPresent(applicantCorrespondent::setTelephone);
                correspondents.add(applicantCorrespondent);
            } else if (applicantType.equals("AGENT")) {
                ComplaintCorrespondent applicantCorrespondent = new ComplaintCorrespondent(ctx.read(AGENT_APPLICANT_NAME), CorrespondentType.COMPLAINANT);
                ComplaintCorrespondent agentCorrespondent = new ComplaintCorrespondent(ctx.read(AGENT_AGENT_NAME), CorrespondentType.THIRD_PARTY_REP);
                optionalString(ctx, AGENT_AGENT_EMAIL).ifPresent(agentCorrespondent::setEmail);
                optionalString(ctx, AGENT_AGENT_PHONE).ifPresent(agentCorrespondent::setTelephone);
                //First correspondent added becomes the primary correspondent for the case.
                correspondents.add(applicantCorrespondent);
                correspondents.add(agentCorrespondent);
            } else {
                throw new IllegalStateException("APPLICANT_TYPE Unknown : " + applicantType);
            }
        } catch (PathNotFoundException e) {
            log.info("getComplaintCorrespondent, no correspondents found for case.");
        }

        return correspondents;
    }

    @Override
    public String getFormattedDocument() {
        String formattedText = jsonBody; // Fall back if conversion fails
        try {
            JSONToSimpleTextConverter jsonToSimpleTextConverter = new JSONToSimpleTextConverter(jsonBody);
            formattedText = jsonToSimpleTextConverter.getConvertedOutput();
        } catch (IOException e) {
            log.warn("Document formatting failed due to : {}", e.getMessage());
        }
        return formattedText;
    }

    @Override
    public String getRawPayload() {
        return jsonBody;
    }

    private Optional<String> optionalString(ReadContext ctx, String path) {
        try {
            String value = ctx.read(path);
            return Optional.of(value);
        } catch (PathNotFoundException e) {
            return Optional.empty();
        }
    }
}