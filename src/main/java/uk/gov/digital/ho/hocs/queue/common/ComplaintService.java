package uk.gov.digital.ho.hocs.queue.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.client.audit.AuditClient;
import uk.gov.digital.ho.hocs.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ComplaintService {

    public static final String CORRESPONDENTS_LABEL = "Correspondents";
    public static final String COMPLAINT_TYPE_LABEL = "ComplaintType";
    private final WorkflowClient workflowClient;
    private final CaseworkClient caseworkClient;
    private final ClientContext clientContext;
    private final AuditClient auditClient;

    @Autowired
    public ComplaintService(WorkflowClient workflowClient,
                            CaseworkClient caseworkClient,
                            ClientContext clientContext,
                            AuditClient auditClient) {
        this.workflowClient = workflowClient;
        this.caseworkClient = caseworkClient;
        this.clientContext = clientContext;
        this.auditClient = auditClient;
    }

    public void createComplaint(ComplaintData complaintData, ComplaintTypeData complaintTypeData) throws IOException {

        log.info("createComplaint, started : type {}", complaintData.getComplaintType());

        CreateCaseRequest request = new CreateCaseRequest(complaintTypeData.getCaseType(), complaintData.getDateReceived());
        CreateCaseResponse createCaseResponse = workflowClient.createCase(request);

        UUID caseUUID = createCaseResponse.getUuid();

        log.info("createComplaint, create case : caseUUID : {}, reference : {}", caseUUID, createCaseResponse.getReference());

        UUID stageForCaseUUID = caseworkClient.getStageForCase(caseUUID);

        auditClient.audit(complaintTypeData.getCreateComplaintEventType(), caseUUID, stageForCaseUUID, complaintData.getRawPayload());

        log.info("createComplaint, get stage for case : caseUUID : {}, stageForCaseUUID : {}", caseUUID, stageForCaseUUID);

        caseworkClient.updateStageUser(caseUUID, stageForCaseUUID, UUID.fromString(clientContext.getUserId()));

        caseworkClient.addCorrespondentToCase(caseUUID, stageForCaseUUID, complaintData.getComplaintCorrespondent());

        UUID primaryCorrespondent = caseworkClient.getPrimaryCorrespondent(caseUUID);

        log.info("createComplaint, added primary correspondent : caseUUID : {}, primaryCorrespondent : {}", caseUUID, primaryCorrespondent);

        Map<String, String> correspondents = Map.of(CORRESPONDENTS_LABEL, primaryCorrespondent.toString());

        workflowClient.advanceCase(caseUUID, stageForCaseUUID, correspondents);

        auditClient.audit(complaintTypeData.getCreateCorrespondentEventType(), caseUUID, stageForCaseUUID, correspondents);

        log.info("createComplaint, case advanced for correspondent : caseUUID : {}", caseUUID);

        Map<String, String> complaintType = Map.of(COMPLAINT_TYPE_LABEL, complaintData.getComplaintType());

        workflowClient.advanceCase(caseUUID, stageForCaseUUID, complaintType);

        log.info("createComplaint, case advanced for complaintType : caseUUID : {}", caseUUID);

        log.info("createComplaint, completed : caseUUID : {}", caseUUID);
    }

}
