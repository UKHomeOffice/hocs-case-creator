package uk.gov.digital.ho.hocs.domain.queue.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.LogEvent;
import uk.gov.digital.ho.hocs.application.RequestData;
import uk.gov.digital.ho.hocs.client.document.DocumentClient;
import uk.gov.digital.ho.hocs.client.document.dto.CreateDocumentRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.MigrationCaseworkClient;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseResponse;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCorrespondentRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.MigrationComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.migration.workflow.MigrationWorkflowClient;
import uk.gov.digital.ho.hocs.client.migration.workflow.dto.CreateWorkflowRequest;
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Profile("migration")
public class MigrationService {

    public static final String CHANNEL_LABEL = "Channel";

    private final MigrationCaseworkClient migrationCaseworkClient;

    private final DocumentClient documentClient;

    private final MigrationWorkflowClient workflowClient;

    private final RequestData requestData;

    private final MessageLogService messageLogService;

    private final ObjectMapper objectMapper;

    public MigrationService(
        MigrationCaseworkClient migrationCaseworkClient,
        RequestData requestData,
        ObjectMapper objectMapper,
        MessageLogService messageLogService,
        DocumentClient documentClient,
        MigrationWorkflowClient workflowClient) {
        this.migrationCaseworkClient = migrationCaseworkClient;
        this.requestData = requestData;
        this.objectMapper = objectMapper;
        this.messageLogService = messageLogService;
        this.documentClient = documentClient;
        this.workflowClient = workflowClient;
    }

    public void createMigrationCase(MigrationData migrationCaseData, MigrationCaseTypeData migrationCaseTypeData) {
        UUID caseId;
        UUID stageId;

        // case
        try {
            var migrationRequest = composeMigrateCaseRequest(migrationCaseData, migrationCaseTypeData);
            CreateMigrationCaseResponse createMigrationCaseResponse = migrationCaseworkClient.migrateCase(
                migrationRequest);
            messageLogService.updateCaseUuidAndStatus(
                requestData.getCorrelationId(),
                createMigrationCaseResponse.getUuid(),
                Status.CASE_CREATED
            );
            caseId = createMigrationCaseResponse.getUuid();
            stageId = createMigrationCaseResponse.getStageId();
            log.info("Created migration case {}", createMigrationCaseResponse.getUuid());
        } catch (Exception e) {
            messageLogService.updateStatus(requestData.getCorrelationId(), Status.CASE_MIGRATION_FAILED);
            log.error("Failed to create migration case", e);
            throw new ApplicationExceptions.CaseCreationException(e.getMessage(), LogEvent.CASE_MIGRATION_FAILURE);
        }

        createCorrespondents(caseId, stageId, migrationCaseData);

        createCaseAttachments(caseId, migrationCaseData);

        //case is open if no completed date set
        if(migrationCaseData.getDateCompleted() == null)  {
            try {
                workflowClient.createWorkflow(new CreateWorkflowRequest(caseId));
                log.info("Created workflow for open case {}", caseId);
            } catch (Exception e) {
                messageLogService.updateStatus(requestData.getCorrelationId(), Status.WORKFLOW_MIGRATION_FAILURE);
                log.error("Failed to create workflow for open case {}", caseId, e);
                throw new ApplicationExceptions.CaseCreationException(e.getMessage(), LogEvent.WORKFLOW_MIGRATION_FAILURE);
            }
        }

        messageLogService.updateStatus(requestData.getCorrelationId(), Status.COMPLETED);
    }

    void createCorrespondents(
        UUID caseId,
        UUID stageId,
        MigrationData migrationCaseData
    ) {
        try {
            var migrationCorrespondentRequest = composeMigrationCorrespondentRequest(
                caseId,
                stageId,
                migrationCaseData
            );
            migrationCaseworkClient.migrateCorrespondent(migrationCorrespondentRequest);
            messageLogService.updateCaseUuidAndStatus(
                requestData.getCorrelationId(),
                caseId,
                Status.CASE_CORRESPONDENTS_HANDLED
            );
            log.info("Created correspondents for migrated case {}", caseId);
        } catch (Exception e) {
            log.error("Failed to create correspondents for migrated case {}", caseId, e);
            messageLogService.updateStatus(requestData.getCorrelationId(), Status.CASE_CORRESPONDENTS_FAILED);
            throw new ApplicationExceptions.CaseCorrespondentCreationException(
                e.getMessage(),
                LogEvent.CASE_CORRESPONDENTS_FAILURE
            );
        }
    }

    void createCaseAttachments(UUID caseId, MigrationData migrationCaseData) {
        try {
            var caseAttachments = getCaseAttachments(
                caseId,
                migrationCaseData
            );
            for (CaseAttachment attachment : caseAttachments) {
                CreateDocumentRequest document =
                    new CreateDocumentRequest(
                        attachment.getDisplayName(),
                        attachment.getType(),
                        attachment.getDocumentPath(),
                        caseId
                    );
                documentClient.createDocument(caseId, document);
            }
            messageLogService.updateCaseUuidAndStatus(
                requestData.getCorrelationId(),
                caseId,
                Status.CASE_DOCUMENT_CREATED
            );
            log.info("Created case attachments for migrated case {}", caseId);
        } catch (Exception e) {
            log.error("Failed to create case attachments for migrated case {}", caseId, e);
            messageLogService.updateStatus(requestData.getCorrelationId(), Status.CASE_DOCUMENT_FAILED);
            throw new ApplicationExceptions.DocumentCreationException(
                e.getMessage(),
                LogEvent.CASE_DOCUMENT_CREATION_FAILURE
            );
        }
    }

    CreateMigrationCaseRequest composeMigrateCaseRequest(
        MigrationData migrationData,
        MigrationCaseTypeData migrationCaseTypeData
    ) {
        Map<String, String> initialData = Map.of(CHANNEL_LABEL, migrationCaseTypeData.getOrigin());

        return new CreateMigrationCaseRequest(
            migrationData.getComplaintType(),
            migrationData.getDateCreated(),
            migrationData.getDateReceived(),
            migrationData.getDateCompleted(),
            initialData,
            migrationData.getDateCompleted() != null
                ? StageTypeMapping.getStageType(migrationData.getComplaintType())
                : null,
            migrationData.getMigratedReference()
        );
    }

    CreateMigrationCorrespondentRequest composeMigrationCorrespondentRequest(
        UUID caseId,
        UUID stageId,
        MigrationData migrationData
    ) {
        MigrationComplaintCorrespondent primaryCorrespondent = getPrimaryCorrespondent(migrationData.getPrimaryCorrespondent());
        List<MigrationComplaintCorrespondent> additionalCorrespondents = getAdditionalCorrespondents(caseId, migrationData.getAdditionalCorrespondents());

        return new CreateMigrationCorrespondentRequest(
            caseId,
            stageId,
            primaryCorrespondent,
            additionalCorrespondents
        );
    }

    List<CaseAttachment> getCaseAttachments(UUID caseId, MigrationData migrationData) {
        return getCaseAttachments(caseId, migrationData.getCaseAttachments());
    }

    public List<CaseAttachment> getCaseAttachments(UUID caseId, String attachmentsJson) {
        try {
            List<CaseAttachment> caseAttachments = objectMapper.convertValue(
                objectMapper.readValue(attachmentsJson, JSONArray.class),
                new TypeReference<>() {
                }
            );
            return caseAttachments;
        } catch (Exception e) {
            log.error("Failed to create case attachments for case id {}", caseId, e);
            messageLogService.updateStatus(requestData.getCorrelationId(), Status.CASE_DOCUMENT_FAILED);
            return Collections.emptyList();
        }
    }

    public MigrationComplaintCorrespondent getPrimaryCorrespondent(LinkedHashMap correspondentJson) {
        MigrationComplaintCorrespondent primaryCorrespondent = objectMapper.convertValue(
            correspondentJson,
            new TypeReference<>() {
            }
        );
        return primaryCorrespondent;
    }

    public List<MigrationComplaintCorrespondent> getAdditionalCorrespondents(UUID caseId, Optional<String> correspondentJson) {
        try {
            List<MigrationComplaintCorrespondent> additionalCorrespondents = objectMapper.convertValue(
                objectMapper.readValue(correspondentJson.get(), JSONArray.class),
                new TypeReference<>() {
                }
            );
            return additionalCorrespondents;
        } catch (Exception e) {
            log.error("Failed to create additional correspondents for case id {}", caseId);
            messageLogService.updateStatus(requestData.getCorrelationId(), Status.CASE_ADDITIONAL_CORRESPONDENTS_FAILED);
            return Collections.emptyList();
        }
    }
}
