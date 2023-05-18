package uk.gov.digital.ho.hocs.domain.queue.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.LogEvent;
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
    private final DocumentS3Client documentS3Client;
    private final MessageLogService messageLogService;
    private final String user;
    private final String team;

    @Autowired
    public ComplaintService(WorkflowClient workflowClient,
                            CaseworkClient caseworkClient,
                            DocumentS3Client documentS3Client,
                            MessageLogService messageLogService,
                            @Value("${case.creator.identity.user}") String user,
                            @Value("${case.creator.identity.team}") String team) {
        this.workflowClient = workflowClient;
        this.caseworkClient = caseworkClient;
        this.documentS3Client = documentS3Client;
        this.messageLogService = messageLogService;
        this.user = user;
        this.team = team;
    }

    public void createComplaint(String messageId, ComplaintData complaintData, ComplaintTypeData complaintTypeData) {
        var documentSummary = createDocument(messageId, complaintData);
        var caseUuid = createCase(messageId, complaintData, complaintTypeData, documentSummary);
        var stageForCaseUUID = getCaseStage(messageId, caseUuid);
        updateCaseUser(messageId, caseUuid, stageForCaseUUID);
        updateCaseCorrespondents(messageId, complaintData, caseUuid, stageForCaseUUID);
        updateCaseTeam(messageId, caseUuid, stageForCaseUUID);
    }

    private DocumentSummary createDocument(String messageId, ComplaintData complaintData) {
        try {
            String untrustedS3ObjectName = documentS3Client.storeUntrustedDocument(ORIGINAL_FILENAME, complaintData.getFormattedDocument());
            log.info("Untrusted document stored in {}, for messageId: {}", untrustedS3ObjectName, messageId);
            messageLogService.updateStatus(messageId, Status.CASE_DOCUMENT_CREATED);
            return new DocumentSummary(ORIGINAL_FILENAME, DOCUMENT_TYPE, untrustedS3ObjectName);
        } catch (Exception e) {
            messageLogService.updateStatus(messageId, Status.CASE_DOCUMENT_FAILED);
            throw new ApplicationExceptions.DocumentCreationException(e.getMessage(), LogEvent.CASE_DOCUMENT_CREATION_FAILURE);
        }
    }

    private UUID createCase(String messageId, ComplaintData complaintData, ComplaintTypeData complaintTypeData, DocumentSummary documentSummary) {
        try {
            var createRequest = composeCreateCaseRequest(complaintData, complaintTypeData, documentSummary);
            var createCaseResponse = workflowClient.createCase(messageId, createRequest);
            log.info("createComplaint, create case : caseUUID : {}, reference : {}", createCaseResponse.getUuid(), createCaseResponse.getReference());
            messageLogService.updateCaseUuidAndStatus(messageId, createCaseResponse.getUuid(), Status.CASE_CREATED);
            return createCaseResponse.getUuid();
        } catch (Exception e) {
            messageLogService.updateStatus(messageId, Status.CASE_CREATION_FAILED);
            throw new ApplicationExceptions.CaseCreationException(e.getMessage(), LogEvent.CASE_CREATION_FAILURE);
        }
    }

    private void updateCaseCorrespondents(String messageId, ComplaintData complaintData, UUID caseUuid, UUID stageUuid) {
        try {
            List<ComplaintCorrespondent> correspondentsList = complaintData.getComplaintCorrespondent();
            if (!correspondentsList.isEmpty()) {
                for (ComplaintCorrespondent correspondent : correspondentsList) {
                    caseworkClient.addCorrespondentToCase(messageId, caseUuid, stageUuid, correspondent);
                }

                UUID primaryCorrespondent = caseworkClient.getPrimaryCorrespondent(messageId, caseUuid);
                log.info("createComplaint, added primary correspondent : caseUUID : {}, primaryCorrespondent : {}", caseUuid, primaryCorrespondent);

                caseworkClient.updateCase(messageId, caseUuid, stageUuid, Map.of(CORRESPONDENTS_LABEL, primaryCorrespondent.toString()));
                log.info("createComplaint, case data updated with primary correspondent: caseUUID : {}, primaryCorrespondent : {}", caseUuid, primaryCorrespondent);
            } else {
                log.info("createComplaint, no correspondents added to case : caseUUID : {}", caseUuid);
            }
            messageLogService.updateStatus(messageId, Status.CASE_CORRESPONDENTS_HANDLED);
        } catch (Exception e) {
            messageLogService.updateStatus(messageId, Status.CASE_CORRESPONDENTS_FAILED);
            throw new ApplicationExceptions.CaseCorrespondentCreationException(e.getMessage(), LogEvent.CASE_CORRESPONDENTS_FAILURE);
        }
    }

    private UUID getCaseStage(String messageId, UUID caseUUID) {
        try {
            var stageUuid = caseworkClient.getStageForCase(messageId, caseUUID);
            log.info("createComplaint, get stage for case : caseUUID : {}, stageForCaseUUID : {}", caseUUID, stageUuid);
            messageLogService.updateStatus(messageId, Status.CASE_STAGE_RETRIEVED);
            return stageUuid;
        } catch (Exception e) {
            messageLogService.updateStatus(messageId, Status.CASE_STAGE_RETRIEVAL_FAILED);
            throw new ApplicationExceptions.CaseStageRetrievalException(e.getMessage(), LogEvent.CASE_STAGE_RETRIEVAL_FAILURE);
        }
    }

    private void updateCaseUser(String messageId, UUID caseUuid, UUID stageUuid) {
        try {
            caseworkClient.updateStageUser(messageId, caseUuid, stageUuid, UUID.fromString(user));
            log.info("createComplaint, user updated for case : caseUUID : {}, user : {}", caseUuid, user);
            messageLogService.updateStatus(messageId, Status.CASE_USER_UPDATED);
        } catch (Exception e) {
            messageLogService.updateStatus(messageId, Status.CASE_USER_UPDATE_FAILED);
            throw new ApplicationExceptions.CaseUserUpdateException(e.getMessage(), LogEvent.CASE_USER_UPDATE_FAILURE);
        }
    }

    private void updateCaseTeam(String messageId, UUID caseUuid, UUID stageUuid) {
        try {
            caseworkClient.updateStageTeam(messageId, caseUuid, stageUuid, UUID.fromString(team));
            log.info("createComplaint, team updated for case : caseUUID : {}, teamUUID : {}", caseUuid, team);
            messageLogService.updateStatus(messageId, Status.CASE_TEAM_UPDATED);
        } catch (Exception e) {
            messageLogService.updateStatus(messageId, Status.CASE_TEAM_UPDATE_FAILED);
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
