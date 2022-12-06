package uk.gov.digital.ho.hocs.queue.complaints.ukvi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;
import uk.gov.digital.ho.hocs.queue.complaints.CorrespondentType;
import uk.gov.digital.ho.hocs.queue.data.CaseData;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UKVIComplaintData extends CaseData {

    static final String COMPLAINT_TYPE = "$.complaint.complaintType";
    static final String APPLICANT_TYPE = "$.complaint.reporterDetails.applicantType";
    static final String APPLICANT_APPLICANT_NAME = "$.complaint.reporterDetails.applicantName";
    static final String APPLICANT_APPLICANT_EMAIL = "$.complaint.reporterDetails.applicantEmail";
    static final String APPLICANT_APPLICANT_PHONE = "$.complaint.reporterDetails.applicantPhone";
    static final String AGENT_APPLICANT_NAME = "$.complaint.reporterDetails.applicantDetails.applicantName";
    static final String AGENT_AGENT_NAME = "$.complaint.reporterDetails.agentDetails.agentName";
    static final String AGENT_AGENT_EMAIL = "$.complaint.reporterDetails.agentDetails.agentEmail";
    static final String AGENT_AGENT_PHONE = "$.complaint.reporterDetails.agentDetails.agentPhone";

    public UKVIComplaintData(String jsonBody, ObjectMapper objectMapper, EnumMappingsRepository complaintDetailsRepository) {
        super(jsonBody, objectMapper, complaintDetailsRepository);
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
    public String getComplaintType() {
        return ctx.read(COMPLAINT_TYPE);
    }

}
