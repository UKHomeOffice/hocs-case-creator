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
import uk.gov.digital.ho.hocs.client.workflow.dto.DocumentSummary;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ComplaintService {

    public static final String CORRESPONDENTS_LABEL = "Correspondents";
    public static final String COMPLAINT_TYPE_LABEL = "ComplaintType";
    public static final String WEB_FORM_CONTENT = "WebFormContent.txt";
    public static final String DOCUMENT_TYPE = "To document";
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

    public void createComplaint(ComplaintData complaintData, ComplaintTypeData complaintTypeData) {

        log.info("createComplaint, started : type {}", complaintData.getComplaintType());

        String fakeS3UntrustedUrl = "8bdc5724-80e4-4fe3-a0a9-1f00262107b0";
        DocumentSummary documentSummary = new DocumentSummary(WEB_FORM_CONTENT, DOCUMENT_TYPE, fakeS3UntrustedUrl);

        CreateCaseRequest request = new CreateCaseRequest(complaintTypeData.getCaseType(), complaintData.getDateReceived(), List.of(documentSummary));
        CreateCaseResponse createCaseResponse = workflowClient.createCase(request);

        UUID caseUUID = createCaseResponse.getUuid();

        log.info("createComplaint, create case : caseUUID : {}, reference : {}", caseUUID, createCaseResponse.getReference());

        UUID stageForCaseUUID = caseworkClient.getStageForCase(caseUUID);

        auditClient.audit(complaintTypeData.getCreateComplaintEventType(), caseUUID, stageForCaseUUID);

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
