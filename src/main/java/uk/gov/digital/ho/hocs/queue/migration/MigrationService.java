package uk.gov.digital.ho.hocs.queue.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.application.LogEvent;
import uk.gov.digital.ho.hocs.client.document.DocumentClient;
import uk.gov.digital.ho.hocs.client.document.dto.CreateDocumentRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.MigrationCaseworkClient;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.*;
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.model.Status;
import uk.gov.digital.ho.hocs.service.MessageLogService;

import java.util.*;

@Slf4j
@Service
public class MigrationService {

    public static final String CHANNEL_LABEL = "Channel";

    private final MigrationCaseworkClient migrationCaseworkClient;

    private final DocumentClient documentClient;

    private final ClientContext clientContext;

    private final MessageLogService messageLogService;

    private final ObjectMapper objectMapper;

    public MigrationService(MigrationCaseworkClient migrationCaseworkClient,
                            ClientContext clientContext,
                            ObjectMapper objectMapper,
                            MessageLogService messageLogService,
                            DocumentClient documentClient) {
        this.migrationCaseworkClient = migrationCaseworkClient;
        this.clientContext = clientContext;
        this.objectMapper = objectMapper;
        this.messageLogService = messageLogService;
        this.documentClient = documentClient;
    }

    public void createMigrationCase(MigrationData migrationCaseData, MigrationCaseTypeData migrationCaseTypeData) {
        UUID caseId;
        UUID stageId;
        ResponseEntity<CreateMigrationCaseResponse> caseResponseEntity;
        ResponseEntity correspondentsResponseEntity = null;
        ResponseEntity caseAttachmentsResponseEntity = null;

        // case
        try {
            var migrationRequest = composeMigrateCaseRequest(migrationCaseData, migrationCaseTypeData);
            caseResponseEntity = migrationCaseworkClient.migrateCase(migrationRequest);
            CreateMigrationCaseResponse caseResponse = caseResponseEntity.getBody();
            messageLogService.updateMessageLogEntryCaseUuidAndStatus(
                    clientContext.getCorrelationId(),
                    caseResponse.getUuid(),
                    Status.CASE_CREATED);
            caseId = caseResponse.getUuid();
            stageId = caseResponse.getStageId();
            log.info("Created migration case {}", caseResponse.getUuid());
        } catch (Exception e) {
            messageLogService.updateMessageLogEntryStatus(clientContext.getCorrelationId(), Status.CASE_MIGRATION_FAILED);
            throw new ApplicationExceptions.CaseCreationException(e.getMessage(), LogEvent.CASE_MIGRATION_FAILURE);
        }

        if (hasCreatedCase(caseResponseEntity)) {
            correspondentsResponseEntity = createCorrespondents(caseId, stageId, migrationCaseData);

            if (hasCreatedCorrespondents(correspondentsResponseEntity)) {
                caseAttachmentsResponseEntity = createCaseAttachments(caseId, migrationCaseData);

                if (hasLinkedCaseAttachments(caseAttachmentsResponseEntity)) {
                    completeMigration();
                }
            }
        }

    }

    private boolean hasLinkedCaseAttachments(ResponseEntity caseAttachmentsResponseEntity) {
        return caseAttachmentsResponseEntity.getStatusCode().is2xxSuccessful();
    }

    private boolean hasCreatedCorrespondents(ResponseEntity correspondentsResponseEntity) {
        return correspondentsResponseEntity.getStatusCode().is2xxSuccessful();
    }

    private boolean hasCreatedCase(ResponseEntity<CreateMigrationCaseResponse> caseResponseEntity) {
        return caseResponseEntity.getStatusCode().is2xxSuccessful();
    }

    private ResponseEntity createCorrespondents(UUID caseId,
                                                UUID stageId,
                                                MigrationData migrationCaseData) {
        try {
            var migrationCorrespondentRequest  = composeMigrationCorrespondentRequest(
                    caseId,
                    stageId,
                    migrationCaseData);
            ResponseEntity responseEntity = migrationCaseworkClient.migrateCorrespondent(migrationCorrespondentRequest);
            messageLogService.updateMessageLogEntryCaseUuidAndStatus(clientContext.getCorrelationId(), caseId, Status.CASE_CORRESPONDENTS_HANDLED);
            log.info("Created correspondents for migrated case {}", caseId);
            return responseEntity;
        } catch (Exception e) {
            messageLogService.updateMessageLogEntryStatus(clientContext.getCorrelationId(), Status.CASE_CORRESPONDENTS_FAILED);
            throw new ApplicationExceptions.CaseCorrespondentCreationException(e.getMessage(), LogEvent.CASE_CORRESPONDENTS_FAILURE);
        }
    }

    private ResponseEntity createCaseAttachments(UUID caseId, MigrationData migrationCaseData) {
//        try {
//            var migrationCaseAttachmentRequest = composeMigrationCaseAttachmentRequest(caseId, migrationCaseData);
//            ResponseEntity responseEntity = migrationCaseworkClient.migrateCaseAttachments(migrationCaseAttachmentRequest);
//            log.info("Created case attachments for migrated case {}", caseId);
//            return responseEntity;
//        } catch (Exception e) {
//            messageLogService.updateMessageLogEntryStatus(clientContext.getCorrelationId(), Status.CASE_DOCUMENT_FAILED);
//            throw new ApplicationExceptions.DocumentCreationException(e.getMessage(), LogEvent.CASE_DOCUMENT_CREATION_FAILURE);
//        }
       try {
           var caseAttachments = getCaseAttachments(
                   caseId,
                   migrationCaseData);
           for(CaseAttachment attachment : caseAttachments) {
               CreateDocumentRequest document =
                       new CreateDocumentRequest(
                               attachment.getDisplayName(),
                               attachment.getType(),
                               attachment.getDocumentPath(),
                               caseId);
               documentClient.createDocument(caseId, document);
           }
           messageLogService.updateMessageLogEntryCaseUuidAndStatus(clientContext.getCorrelationId(), caseId, Status.CASE_DOCUMENT_CREATED);
           log.info("Created case attachments for migrated case {}", caseId);
       } catch (Exception e) {
            messageLogService.updateMessageLogEntryStatus(clientContext.getCorrelationId(), Status.CASE_DOCUMENT_FAILED);
            throw new ApplicationExceptions.DocumentCreationException(e.getMessage(), LogEvent.CASE_DOCUMENT_CREATION_FAILURE);
       }
       return ResponseEntity.ok().build();
    }

    private void completeMigration() {
        messageLogService.updateMessageLogEntryStatus(clientContext.getCorrelationId(), Status.COMPLETED);
    }

    CreateMigrationCaseRequest composeMigrateCaseRequest(MigrationData migrationData, MigrationCaseTypeData migrationCaseTypeData) {
        Map<String, String> initialData = Map.of(CHANNEL_LABEL, migrationCaseTypeData.getOrigin());

        return new CreateMigrationCaseRequest(
                migrationData.getComplaintType(),
                migrationData.getDateReceived(),
                initialData,
                "MIGRATION");
    }

    CreateMigrationCorrespondentRequest composeMigrationCorrespondentRequest(UUID caseId, UUID stageId, MigrationData migrationData) {
        MigrationComplaintCorrespondent primaryCorrespondent = getPrimaryCorrespondent(migrationData.getPrimaryCorrespondent());
        List<MigrationComplaintCorrespondent> additionalCorrespondents = getAdditionalCorrespondents(migrationData.getAdditionalCorrespondents());

        return new CreateMigrationCorrespondentRequest(
            caseId,
            stageId,
            primaryCorrespondent,
            additionalCorrespondents
        );
    }

    List<CaseAttachment> getCaseAttachments(UUID caseId, MigrationData migrationData) {
        return getCaseAttachments(migrationData.getCaseAttachments());
    }

    public List<CaseAttachment> getCaseAttachments(String attachmentsJson) {
        try {
            List<CaseAttachment> caseAttachments = objectMapper.convertValue(
                    objectMapper.readValue(attachmentsJson, JSONArray.class),
                    new TypeReference<>() {
                    });
            return caseAttachments;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public MigrationComplaintCorrespondent getPrimaryCorrespondent(LinkedHashMap correspondentJson) {
        MigrationComplaintCorrespondent primaryCorrespondent = objectMapper.convertValue(
                correspondentJson,
                new TypeReference<>() {
                });
        return primaryCorrespondent;
    }

    public List<MigrationComplaintCorrespondent> getAdditionalCorrespondents(Optional<String> correspondentJson)  {
        try {
            List<MigrationComplaintCorrespondent> additionalCorrespondents = objectMapper.convertValue(
                    objectMapper.readValue(correspondentJson.get(), JSONArray.class),
                    new TypeReference<>() {
                    });
            return additionalCorrespondents;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }


}
