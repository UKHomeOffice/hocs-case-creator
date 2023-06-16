package uk.gov.digital.ho.hocs.domain.queue.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.application.LogEvent;
import uk.gov.digital.ho.hocs.client.document.DocumentClient;
import uk.gov.digital.ho.hocs.client.document.dto.CreateDocumentRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.MigrationCaseworkClient;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseResponse;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCorrespondentRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreatePrimaryTopicRequest;
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
    public static final String EXISTING_CASE_ID_PATH = "$.existing_case_id";

    private final MigrationCaseworkClient migrationCaseworkClient;

    private final DocumentClient documentClient;

    private final MigrationWorkflowClient workflowClient;

    private final CaseDataService caseDataService;

    private final MessageLogService messageLogService;

    private final ObjectMapper objectMapper;

    private final TopicMapper topicMapper;

    public MigrationService(
        MigrationCaseworkClient migrationCaseworkClient,
        ObjectMapper objectMapper,
        MessageLogService messageLogService,
        DocumentClient documentClient,
        MigrationWorkflowClient workflowClient,
        CaseDataService caseDataService,
        TopicMapper topicMapper
    ) {
        this.migrationCaseworkClient = migrationCaseworkClient;
        this.objectMapper = objectMapper;
        this.messageLogService = messageLogService;
        this.documentClient = documentClient;
        this.workflowClient = workflowClient;
        this.caseDataService = caseDataService;
        this.topicMapper = topicMapper;
    }

    public Status createMigrationCase(String messageId, MigrationData migrationCaseData) {
        UUID caseId;
        UUID stageId;

        // case
        try {
            var migrationRequest = composeMigrateCaseRequest(messageId, migrationCaseData);
            CreateMigrationCaseResponse createMigrationCaseResponse = migrationCaseworkClient.migrateCase(
                    messageId,
                    migrationRequest);
            messageLogService.updateCaseUuidAndStatus(
                    messageId,
                    createMigrationCaseResponse.getUuid(),
                    Status.CASE_CREATED
            );
            caseId = createMigrationCaseResponse.getUuid();
            stageId = createMigrationCaseResponse.getStageId();
            log.info("Created migration case {}", createMigrationCaseResponse.getUuid());
        } catch (HttpClientErrorException.Conflict e) {
            log.warn(
                "Case was not migrated, a case with migrated reference {} already exists with caseId: {}",
                migrationCaseData.getMigratedReference(),
                readExistingCaseId(e.getResponseBodyAsString())
            );
            return Status.DUPLICATE_MIGRATED_REFERENCE;
        } catch (Exception e) {
            messageLogService.updateStatus(messageId, Status.CASE_MIGRATION_FAILED);
            log.error("Failed to create migration case", e);
            throw new ApplicationExceptions.CaseCreationException(e.getMessage(), LogEvent.CASE_MIGRATION_FAILURE);
        }

        createCorrespondents(messageId, caseId, stageId, migrationCaseData);

        createCaseAttachments(messageId, caseId, migrationCaseData);

        //case is open if no completed date set
        if(migrationCaseData.getDateCompleted() == null)  {
            try {
                workflowClient.createWorkflow(messageId, new CreateWorkflowRequest(caseId));
                log.info("Created workflow for open case {}", caseId);
            } catch (Exception e) {
                messageLogService.updateStatus(messageId, Status.WORKFLOW_MIGRATION_FAILURE);
                log.error("Failed to create workflow for open case {}", caseId, e);
                throw new ApplicationExceptions.CaseCreationException(
                    e.getMessage(),
                    LogEvent.WORKFLOW_MIGRATION_FAILURE
                );
            }
        }

        return Status.COMPLETED;
    }

    private static String readExistingCaseId(String responseBody) {
        try {
            return JsonPath.read(responseBody, EXISTING_CASE_ID_PATH);
        } catch (Exception ignored) {
            return null;
        }
    }

    void createCorrespondents(
            String messageId,
            UUID caseId,
            UUID stageId,
            MigrationData migrationCaseData
    ) {
        try {
            var migrationCorrespondentRequest = composeMigrationCorrespondentRequest(
                    messageId,
                    caseId,
                    stageId,
                    migrationCaseData
            );
            migrationCaseworkClient.migrateCorrespondent(messageId, migrationCorrespondentRequest);
            messageLogService.updateCaseUuidAndStatus(
                    messageId,
                    caseId,
                    Status.CASE_CORRESPONDENTS_HANDLED
            );
            log.info("Created correspondents for migrated case {}", caseId);
        } catch (Exception e) {
            log.error("Failed to create correspondents for migrated case {}", caseId, e);
            messageLogService.updateStatus(messageId, Status.CASE_CORRESPONDENTS_FAILED);
            throw new ApplicationExceptions.CaseCorrespondentCreationException(
                e.getMessage(),
                LogEvent.CASE_CORRESPONDENTS_FAILURE
            );
        }
    }

    void createCaseAttachments(String messageId, UUID caseId, MigrationData migrationCaseData) {
        try {
            var caseAttachments = getCaseAttachments(
                    messageId,
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
                documentClient.createDocument(messageId, document);
            }
            messageLogService.updateCaseUuidAndStatus(
                    messageId,
                    caseId,
                    Status.CASE_DOCUMENT_CREATED
            );
            log.info("Created case attachments for migrated case {}", caseId);
        } catch (Exception e) {
            log.error("Failed to create case attachments for migrated case {}", caseId, e);
            messageLogService.updateStatus(messageId, Status.CASE_DOCUMENT_FAILED);
            throw new ApplicationExceptions.DocumentCreationException(
                e.getMessage(),
                LogEvent.CASE_DOCUMENT_CREATION_FAILURE
            );
        }
    }

    @SuppressWarnings("unused") // HOCS-6385 no longer requires this but may be useful for Alfresco migration
    private void addPrimaryTopic(String messageId, UUID caseId, UUID stageId, MigrationData migrationCaseData) {
        if (stageId == null) {
            log.info("Skipping adding topic for case {} - no stage was available", caseId);
            return;
        }

        if (migrationCaseData.getPrimaryTopic().isEmpty()) {
            log.info("Skipping adding topic for case {} - no primary topic was provided", caseId);
            return;
        }

        try {
            UUID topicId =
                migrationCaseData
                    .getPrimaryTopic()
                    .flatMap(text -> topicMapper.getTopicId(messageId, text))
                    .orElseThrow(() -> new Exception("Topic text did not match a CMS migration topic"));

            migrationCaseworkClient.createPrimaryTopic(
                    messageId,
                    new CreatePrimaryTopicRequest(caseId, stageId, topicId)
            );

            log.info("Created primary topic for migrated case {}", caseId);
        } catch (Exception e) {
            log.warn("Failed to create primary topic for migrated case {} - {}", caseId, e.getMessage(), e);
        }
    }

    CreateMigrationCaseRequest composeMigrateCaseRequest(
        String messageId,
        MigrationData migrationData
    ) {
        Map<String, String> initialData =
            caseDataService.parseCaseDataJson(messageId, migrationData.getCaseDataJson());

        return new CreateMigrationCaseRequest(
            migrationData.getComplaintType(),
            migrationData.getDateCreated(),
            migrationData.getDateReceived(),
            migrationData.getCaseDeadline(),
            migrationData.getDateCompleted(),
            initialData,
            migrationData.getDateCompleted() != null
                ? StageTypeMapping.getStageType(migrationData.getComplaintType())
                : null,
            migrationData.getMigratedReference()
        );
    }

    CreateMigrationCorrespondentRequest composeMigrationCorrespondentRequest(
            String messageId,
            UUID caseId,
            UUID stageId,
            MigrationData migrationData
    ) {
        MigrationComplaintCorrespondent primaryCorrespondent = getPrimaryCorrespondent(migrationData.getPrimaryCorrespondent());
        List<MigrationComplaintCorrespondent> additionalCorrespondents = getAdditionalCorrespondents(messageId, caseId, migrationData.getAdditionalCorrespondents());

        return new CreateMigrationCorrespondentRequest(
            caseId,
            stageId,
            primaryCorrespondent,
            additionalCorrespondents
        );
    }

    List<CaseAttachment> getCaseAttachments(String messageId, UUID caseId, MigrationData migrationData) {
        return getCaseAttachments(messageId, caseId, migrationData.getCaseAttachments());
    }

    public List<CaseAttachment> getCaseAttachments(String messageId, UUID caseId, String attachmentsJson) {
        try {
            return objectMapper.convertValue(
                objectMapper.readValue(attachmentsJson, JSONArray.class),
                new TypeReference<>() {
                }
            );
        } catch (Exception e) {
            log.error("Failed to create case attachments for case id {}", caseId, e);
            messageLogService.updateStatus(messageId, Status.CASE_DOCUMENT_FAILED);
            return Collections.emptyList();
        }
    }

    public MigrationComplaintCorrespondent getPrimaryCorrespondent(LinkedHashMap<String, Object> correspondentJson) {
        return objectMapper.convertValue(
            correspondentJson,
            new TypeReference<>() {
            }
        );
    }

    public List<MigrationComplaintCorrespondent> getAdditionalCorrespondents(String messageId, UUID caseId, Optional<String> correspondentJson) {
        try {
            return objectMapper.convertValue(
                objectMapper.readValue(correspondentJson.orElse("[]"), JSONArray.class),
                new TypeReference<>() {
                }
            );
        } catch (Exception e) {
            log.error("Failed to create additional correspondents for case id {}", caseId);
            messageLogService.updateStatus(messageId, Status.CASE_ADDITIONAL_CORRESPONDENTS_FAILED);
            return Collections.emptyList();
        }
    }
}
