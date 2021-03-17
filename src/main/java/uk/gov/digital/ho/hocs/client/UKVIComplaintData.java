package uk.gov.digital.ho.hocs.client;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;

import java.time.LocalDate;
import java.util.Optional;

public class UKVIComplaintData implements ComplaintData {

    public static final String CREATION_DATE = "$.creationDate";
    public static final String COMPLAINT_TYPE = "$.complaint.complaintType";
    public static final String APPLICANT_TYPE = "$.complaint.reporterDetails.applicantType";
    public static final String APPLICANT_APPLICANT_NAME = "$.complaint.reporterDetails.applicantName";
    public static final String APPLICANT_APPLICANT_EMAIL = "$.complaint.reporterDetails.applicantEmail";
    public static final String APPLICANT_APPLICANT_PHONE = "$.complaint.reporterDetails.applicantPhone";
    public static final String AGENT_APPLICANT_NAME = "$.complaint.reporterDetails.applicantDetails.applicantName";

    private final ReadContext ctx;

    public UKVIComplaintData(String jsonBody) {
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
    public ComplaintCorrespondent getComplaintCorrespondent() {
        String applicantType = ctx.read(APPLICANT_TYPE);
        ComplaintCorrespondent correspondent;

        if (applicantType.equals("APPLICANT")) {
            correspondent = new ComplaintCorrespondent(ctx.read(APPLICANT_APPLICANT_NAME));
            optionalString(ctx, APPLICANT_APPLICANT_EMAIL).ifPresent(correspondent::setEmail);
            optionalString(ctx, APPLICANT_APPLICANT_PHONE).ifPresent(correspondent::setTelephone);

        } else if (applicantType.equals("AGENT")) {
            correspondent = new ComplaintCorrespondent(ctx.read(AGENT_APPLICANT_NAME));

        } else {
            throw new IllegalStateException("APPLICANT_TYPE Unknown : " + applicantType);
        }
        return correspondent;
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
