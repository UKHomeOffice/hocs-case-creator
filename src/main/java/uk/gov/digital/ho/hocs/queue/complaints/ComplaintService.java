package uk.gov.digital.ho.hocs.queue.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.document.DocumentS3Client;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.client.workflow.dto.DocumentSummary;

import java.util.*;

@Slf4j
@Service
public class ComplaintService {

    public static final String CORRESPONDENTS_LABEL = "Correspondents";
    public static final String COMPLAINT_TYPE_LABEL = "ComplaintType";
    public static final String CHANNEL_LABEL = "Channel";
    public static final String ORIGINAL_FILENAME = "WebFormContent.txt";
    public static final String DOCUMENT_TYPE = "To document";
    private final WorkflowClient workflowClient;
    private final CaseworkClient caseworkClient;
    private final ClientContext clientContext;
    private final DocumentS3Client documentS3Client;

    @Autowired
    public ComplaintService(WorkflowClient workflowClient,
                            CaseworkClient caseworkClient,
                            ClientContext clientContext,
                            DocumentS3Client documentS3Client) {
        this.workflowClient = workflowClient;
        this.caseworkClient = caseworkClient;
        this.clientContext = clientContext;
        this.documentS3Client = documentS3Client;
    }

    public void createComplaint(ComplaintData complaintData, ComplaintTypeData complaintTypeData) {

        log.info("createComplaint, started : type {}", complaintData.getComplaintType());

        String untrustedS3ObjectName = documentS3Client.storeUntrustedDocument(ORIGINAL_FILENAME, complaintData.getFormattedDocument());

        log.info("Untrusted document stored in {}, for messageId: {}", untrustedS3ObjectName, clientContext.getCorrelationId());

        DocumentSummary documentSummary = new DocumentSummary(ORIGINAL_FILENAME, DOCUMENT_TYPE, untrustedS3ObjectName);

        var createRequest = composeCreateCaseRequest(complaintData, complaintTypeData, documentSummary);
        CreateCaseResponse createCaseResponse = workflowClient.createCase(createRequest);

        UUID caseUUID = createCaseResponse.getUuid();

        log.info("createComplaint, create case : caseUUID : {}, reference : {}", caseUUID, createCaseResponse.getReference());

        /*
            We can swallow exceptions from this point onward, because we would prefer to reduce duplicates. And by this
            point we have the document stored, and the case is created.
         */
        try {
            UUID stageForCaseUUID = caseworkClient.getStageForCase(caseUUID);
            log.info("createComplaint, get stage for case : caseUUID : {}, stageForCaseUUID : {}", caseUUID, stageForCaseUUID);

            caseworkClient.updateStageUser(caseUUID, stageForCaseUUID, UUID.fromString(clientContext.getUserId()));

            List<ComplaintCorrespondent> correspondentsList = complaintData.getComplaintCorrespondent();
            if (!correspondentsList.isEmpty()) {
                for (ComplaintCorrespondent correspondent : correspondentsList) {
                    caseworkClient.addCorrespondentToCase(caseUUID, stageForCaseUUID, correspondent);
                }

                UUID primaryCorrespondent = caseworkClient.getPrimaryCorrespondent(caseUUID);
                log.info("createComplaint, added primary correspondent : caseUUID : {}, primaryCorrespondent : {}", caseUUID, primaryCorrespondent);

                caseworkClient.updateCase(caseUUID, stageForCaseUUID, Map.of(CORRESPONDENTS_LABEL, primaryCorrespondent.toString()));
                log.info("createComplaint, case data updated with primary correspondent: caseUUID : {}, primaryCorrespondent : {}", caseUUID, complaintData.getComplaintType());
            } else {
                log.info("createComplaint, no correspondents added to case : caseUUID : {}", caseUUID);
            }

            caseworkClient.updateStageTeam(caseUUID, stageForCaseUUID, UUID.fromString(clientContext.getTeamId()));
            log.info("createComplaint, team updated for case : caseUUID : {}, teamUUID : {}", caseUUID, clientContext.getTeamId());

            log.info("createComplaint, completed : caseUUID : {}", caseUUID);
        } catch (Exception e) {
            log.warn("Partial case creation for messageId: {}, caseId:{}, untrusted s3 name :{}", clientContext.getCorrelationId(), caseUUID, untrustedS3ObjectName);
            log.warn(e.getMessage());
        }
    }

    private CreateCaseRequest composeCreateCaseRequest(ComplaintData complaintData, ComplaintTypeData complaintTypeData, DocumentSummary documentSummary) {
        Map<String, String> initialData = Map.of(
                COMPLAINT_TYPE_LABEL, complaintData.getComplaintType(),
                CHANNEL_LABEL, complaintTypeData.getOrigin());

        return new CreateCaseRequest(complaintTypeData.getCaseType(), complaintData.getDateReceived(), List.of(documentSummary), initialData);
    }

}
