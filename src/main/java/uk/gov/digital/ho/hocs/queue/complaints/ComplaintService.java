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
    public static final String ORIGINATED_FROM_LABEL = "XOriginatedFrom";
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
        var documentSummary = createDocument(complaintData);
        var caseUuid = createCase(complaintData, complaintTypeData, documentSummary);

        /*
            We can swallow exceptions from this point onward, because we would prefer to reduce duplicates. And by this
            point we have the document stored, and the case is created.
         */
        try {
            var stageForCaseUUID = getCaseStage(caseUuid);
            updateCaseUser(caseUuid, stageForCaseUUID);
            updateCaseCorrespondents(complaintData, caseUuid, stageForCaseUUID);
            updateCaseTeam(caseUuid, stageForCaseUUID);
        } catch (Exception e) {
            log.warn("Partial case creation for messageId: {}, caseId:{}", clientContext.getCorrelationId(), caseUuid);
            log.warn(e.getMessage());
        }
    }

    private DocumentSummary createDocument(ComplaintData complaintData) {
        String untrustedS3ObjectName = documentS3Client.storeUntrustedDocument(ORIGINAL_FILENAME, complaintData.getFormattedDocument());
        log.info("Untrusted document stored in {}, for messageId: {}", untrustedS3ObjectName, clientContext.getCorrelationId());
        return new DocumentSummary(ORIGINAL_FILENAME, DOCUMENT_TYPE, untrustedS3ObjectName);
    }

    private UUID createCase(ComplaintData complaintData, ComplaintTypeData complaintTypeData, DocumentSummary documentSummary) {
        var createRequest = composeCreateCaseRequest(complaintData, complaintTypeData, documentSummary);
        var createCaseResponse = workflowClient.createCase(createRequest);
        log.info("createComplaint, create case : caseUUID : {}, reference : {}", createCaseResponse.getUuid(), createCaseResponse.getReference());
        return createCaseResponse.getUuid();
    }

    private void updateCaseCorrespondents(ComplaintData complaintData, UUID caseUuid, UUID stageUuid) {
        List<ComplaintCorrespondent> correspondentsList = complaintData.getComplaintCorrespondent();
        if (!correspondentsList.isEmpty()) {
            for (ComplaintCorrespondent correspondent : correspondentsList) {
                caseworkClient.addCorrespondentToCase(caseUuid, stageUuid, correspondent);
            }

            UUID primaryCorrespondent = caseworkClient.getPrimaryCorrespondent(caseUuid);
            log.info("createComplaint, added primary correspondent : caseUUID : {}, primaryCorrespondent : {}", caseUuid, primaryCorrespondent);

            caseworkClient.updateCase(caseUuid, stageUuid, Map.of(CORRESPONDENTS_LABEL, primaryCorrespondent.toString()));
            log.info("createComplaint, case data updated with primary correspondent: caseUUID : {}, primaryCorrespondent : {}", caseUuid, complaintData.getComplaintType());
        } else {
            log.info("createComplaint, no correspondents added to case : caseUUID : {}", caseUuid);
        }
    }

    private UUID getCaseStage(UUID caseUUID) {
        var stageUuid = caseworkClient.getStageForCase(caseUUID);
        log.info("createComplaint, get stage for case : caseUUID : {}, stageForCaseUUID : {}", caseUUID, stageUuid);
        return stageUuid;
    }

    private void updateCaseUser(UUID caseUuid, UUID stageUuid) {
        caseworkClient.updateStageUser(caseUuid, stageUuid, UUID.fromString(clientContext.getUserId()));
        log.info("createComplaint, user updated for case : caseUUID : {}, user : {}", caseUuid, clientContext.getUserId());
    }

    private void updateCaseTeam(UUID caseUuid, UUID stageUuid) {
        caseworkClient.updateStageTeam(caseUuid, stageUuid, UUID.fromString(clientContext.getTeamId()));
        log.info("createComplaint, team updated for case : caseUUID : {}, teamUUID : {}", caseUuid, clientContext.getTeamId());
    }

    private CreateCaseRequest composeCreateCaseRequest(ComplaintData complaintData, ComplaintTypeData complaintTypeData, DocumentSummary documentSummary) {
        Map<String, String> initialData = Map.of(
                COMPLAINT_TYPE_LABEL, complaintData.getComplaintType(),
                CHANNEL_LABEL, complaintTypeData.getOrigin(),
                ORIGINATED_FROM_LABEL, complaintTypeData.getOrigin());

        return new CreateCaseRequest(complaintTypeData.getCaseType(), complaintData.getDateReceived(), List.of(documentSummary), initialData);
    }

}
