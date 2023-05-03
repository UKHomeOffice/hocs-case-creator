package uk.gov.digital.ho.hocs.domain.queue.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.LogEvent;
import uk.gov.digital.ho.hocs.application.MessageContext;
import uk.gov.digital.ho.hocs.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.document.DocumentS3Client;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private final MessageContext messageContext;
    private final DocumentS3Client documentS3Client;
    private final MessageLogService messageLogService;

    @Autowired
    public ComplaintService(WorkflowClient workflowClient,
                            CaseworkClient caseworkClient,
                            MessageContext messageContext,
                            DocumentS3Client documentS3Client,
                            MessageLogService messageLogService) {
        this.workflowClient = workflowClient;
        this.caseworkClient = caseworkClient;
        this.messageContext = messageContext;
        this.documentS3Client = documentS3Client;
        this.messageLogService = messageLogService;
    }

    public void createComplaint(ComplaintData complaintData, ComplaintTypeData complaintTypeData) {
        var documentSummary = createDocument(complaintData);
        var caseUuid = createCase(complaintData, complaintTypeData, documentSummary);
        var stageForCaseUUID = getCaseStage(caseUuid);
        updateCaseUser(caseUuid, stageForCaseUUID);
        updateCaseCorrespondents(complaintData, caseUuid, stageForCaseUUID);
        updateCaseTeam(caseUuid, stageForCaseUUID);
    }

    private DocumentSummary createDocument(ComplaintData complaintData) {
        try {
            String untrustedS3ObjectName = documentS3Client.storeUntrustedDocument(ORIGINAL_FILENAME, complaintData.getFormattedDocument());
            log.info("Untrusted document stored in {}, for messageId: {}", untrustedS3ObjectName, messageContext.getCorrelationId());
            messageLogService.updateStatus(messageContext.getCorrelationId(), Status.CASE_DOCUMENT_CREATED);
            return new DocumentSummary(ORIGINAL_FILENAME, DOCUMENT_TYPE, untrustedS3ObjectName);
        } catch (Exception e) {
            messageLogService.updateStatus(messageContext.getCorrelationId(), Status.CASE_DOCUMENT_FAILED);
            throw new ApplicationExceptions.DocumentCreationException(e.getMessage(), LogEvent.CASE_DOCUMENT_CREATION_FAILURE);
        }
    }

    private UUID createCase(ComplaintData complaintData, ComplaintTypeData complaintTypeData, DocumentSummary documentSummary) {
        try {
            var createRequest = composeCreateCaseRequest(complaintData, complaintTypeData, documentSummary);
            var createCaseResponse = workflowClient.createCase(createRequest);
            log.info("createComplaint, create case : caseUUID : {}, reference : {}", createCaseResponse.getUuid(), createCaseResponse.getReference());
            messageLogService.updateCaseUuidAndStatus(messageContext.getCorrelationId(), createCaseResponse.getUuid(), Status.CASE_CREATED);
            return createCaseResponse.getUuid();
        } catch (Exception e) {
            messageLogService.updateStatus(messageContext.getCorrelationId(), Status.CASE_CREATION_FAILED);
            throw new ApplicationExceptions.CaseCreationException(e.getMessage(), LogEvent.CASE_CREATION_FAILURE);
        }
    }

    private void updateCaseCorrespondents(ComplaintData complaintData, UUID caseUuid, UUID stageUuid) {
        try {
            List<ComplaintCorrespondent> correspondentsList = complaintData.getComplaintCorrespondent();
            if (!correspondentsList.isEmpty()) {
                for (ComplaintCorrespondent correspondent : correspondentsList) {
                    caseworkClient.addCorrespondentToCase(caseUuid, stageUuid, correspondent);
                }

                UUID primaryCorrespondent = caseworkClient.getPrimaryCorrespondent(caseUuid);
                log.info("createComplaint, added primary correspondent : caseUUID : {}, primaryCorrespondent : {}", caseUuid, primaryCorrespondent);

                caseworkClient.updateCase(caseUuid, stageUuid, Map.of(CORRESPONDENTS_LABEL, primaryCorrespondent.toString()));
                log.info("createComplaint, case data updated with primary correspondent: caseUUID : {}, primaryCorrespondent : {}", caseUuid, primaryCorrespondent);
            } else {
                log.info("createComplaint, no correspondents added to case : caseUUID : {}", caseUuid);
            }
            messageLogService.updateStatus(messageContext.getCorrelationId(), Status.CASE_CORRESPONDENTS_HANDLED);
        } catch (Exception e) {
            messageLogService.updateStatus(messageContext.getCorrelationId(), Status.CASE_CORRESPONDENTS_FAILED);
            throw new ApplicationExceptions.CaseCorrespondentCreationException(e.getMessage(), LogEvent.CASE_CORRESPONDENTS_FAILURE);
        }
    }

    private UUID getCaseStage(UUID caseUUID) {
        try {
            var stageUuid = caseworkClient.getStageForCase(caseUUID);
            log.info("createComplaint, get stage for case : caseUUID : {}, stageForCaseUUID : {}", caseUUID, stageUuid);
            messageLogService.updateStatus(messageContext.getCorrelationId(), Status.CASE_STAGE_RETRIEVED);
            return stageUuid;
        } catch (Exception e) {
            messageLogService.updateStatus(messageContext.getCorrelationId(), Status.CASE_STAGE_RETRIEVAL_FAILED);
            throw new ApplicationExceptions.CaseStageRetrievalException(e.getMessage(), LogEvent.CASE_STAGE_RETRIEVAL_FAILURE);
        }
    }

    private void updateCaseUser(UUID caseUuid, UUID stageUuid) {
        try {
            caseworkClient.updateStageUser(caseUuid, stageUuid, UUID.fromString(messageContext.getUserId()));
            log.info("createComplaint, user updated for case : caseUUID : {}, user : {}", caseUuid, messageContext.getUserId());
            messageLogService.updateStatus(messageContext.getCorrelationId(), Status.CASE_USER_UPDATED);
        } catch (Exception e) {
            messageLogService.updateStatus(messageContext.getCorrelationId(), Status.CASE_USER_UPDATE_FAILED);
            throw new ApplicationExceptions.CaseUserUpdateException(e.getMessage(), LogEvent.CASE_USER_UPDATE_FAILURE);
        }
    }

    private void updateCaseTeam(UUID caseUuid, UUID stageUuid) {
        try {
            caseworkClient.updateStageTeam(caseUuid, stageUuid, UUID.fromString(messageContext.getTeamId()));
            log.info("createComplaint, team updated for case : caseUUID : {}, teamUUID : {}", caseUuid, messageContext.getTeamId());
            messageLogService.updateStatus(messageContext.getCorrelationId(), Status.CASE_TEAM_UPDATED);
        } catch (Exception e) {
            messageLogService.updateStatus(messageContext.getCorrelationId(), Status.CASE_TEAM_UPDATE_FAILED);
            throw new ApplicationExceptions.CaseTeamUpdateException(e.getMessage(), LogEvent.CASE_TEAM_UPDATE_FAILURE);
        }
    }

    private CreateCaseRequest composeCreateCaseRequest(ComplaintData complaintData, ComplaintTypeData complaintTypeData, DocumentSummary documentSummary) {
        Map<String, String> initialData = Map.of(
                COMPLAINT_TYPE_LABEL, complaintData.getComplaintType(),
                CHANNEL_LABEL, complaintTypeData.getOrigin(),
                ORIGINATED_FROM_LABEL, complaintTypeData.getOrigin());

        return new CreateCaseRequest(complaintTypeData.getCaseType(), complaintData.getDateReceived(), List.of(documentSummary), initialData);
    }

}
