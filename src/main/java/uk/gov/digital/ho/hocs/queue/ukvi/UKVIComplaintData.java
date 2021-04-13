package uk.gov.digital.ho.hocs.queue.ukvi;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.document.JSONToSimpleTextConverter;
import uk.gov.digital.ho.hocs.queue.common.ComplaintData;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
public class UKVIComplaintData implements ComplaintData {

    public static final String CREATION_DATE = "$.creationDate";
    public static final String COMPLAINT_TYPE = "$.complaint.complaintType";
    public static final String APPLICANT_TYPE = "$.complaint.reporterDetails.applicantType";
    public static final String APPLICANT_APPLICANT_NAME = "$.complaint.reporterDetails.applicantName";
    public static final String APPLICANT_APPLICANT_EMAIL = "$.complaint.reporterDetails.applicantEmail";
    public static final String APPLICANT_APPLICANT_PHONE = "$.complaint.reporterDetails.applicantPhone";
    public static final String AGENT_APPLICANT_NAME = "$.complaint.reporterDetails.applicantDetails.applicantName";
    public static final String AGENT_AGENT_NAME = "$.complaint.reporterDetails.agentDetails.agentName";
    public static final String AGENT_AGENT_EMAIL = "$.complaint.reporterDetails.agentDetails.agentEmail";
    public static final String AGENT_AGENT_TYPE = "$.complaint.reporterDetails.agentDetails.agentType";

    private final ReadContext ctx;
    private final String jsonBody;

    public UKVIComplaintData(String jsonBody) {
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
    public ArrayList<ComplaintCorrespondent> getComplaintCorrespondent() {
        String applicantType = ctx.read(APPLICANT_TYPE);
        ArrayList<ComplaintCorrespondent> correspondents = new ArrayList<>();

        if (applicantType.equals("APPLICANT")) {
            ComplaintCorrespondent applicantCorrespondent = new ComplaintCorrespondent(ctx.read(APPLICANT_APPLICANT_NAME), "COMPLAINANT");
            optionalString(ctx, APPLICANT_APPLICANT_EMAIL).ifPresent(applicantCorrespondent::setEmail);
            optionalString(ctx, APPLICANT_APPLICANT_PHONE).ifPresent(applicantCorrespondent::setTelephone);
            correspondents.add(applicantCorrespondent);
        } else if (applicantType.equals("AGENT")) {
            ComplaintCorrespondent applicantCorrespondent = new ComplaintCorrespondent(ctx.read(AGENT_APPLICANT_NAME), "COMPLAINANT");

            ComplaintCorrespondent agentCorrespondent = new ComplaintCorrespondent(ctx.read(AGENT_AGENT_NAME), ctx.read(AGENT_AGENT_TYPE));
            optionalString(ctx, AGENT_AGENT_EMAIL).ifPresent(agentCorrespondent::setEmail);

            correspondents.add(applicantCorrespondent);
            correspondents.add(agentCorrespondent);
        } else {
            throw new IllegalStateException("APPLICANT_TYPE Unknown : " + applicantType);
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
