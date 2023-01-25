package uk.gov.digital.ho.hocs.queue.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.client.casework.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.client.migration.casework.MigrationCaseworkClient;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.client.document.DocumentS3Client;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.MigrationComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;
import uk.gov.digital.ho.hocs.domain.model.MessageLog;
import uk.gov.digital.ho.hocs.domain.model.Status;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class MigrationService {

    public static final String CHANNEL_LABEL = "Channel";

    private final WorkflowClient workflowClient;
    private final MigrationCaseworkClient migrationCaseworkClient;
    private final ClientContext clientContext;
    private final DocumentS3Client documentS3Client;

    private final ObjectMapper objectMapper;

    private final MigrationStateService migrationStateService;

    public MigrationService(WorkflowClient workflowClient,
                            MigrationCaseworkClient migrationCaseworkClient,
                            ClientContext clientContext,
                            DocumentS3Client documentS3Client,
                            ObjectMapper objectMapper,
                            MigrationStateService migrationStateService) {
        this.workflowClient = workflowClient;
        this.migrationCaseworkClient = migrationCaseworkClient;
        this.clientContext = clientContext;
        this.documentS3Client = documentS3Client;
        this.objectMapper = objectMapper;
        this.migrationStateService = migrationStateService;
    }

    public void createMigrationCase(MigrationData migrationCaseData, MigrationCaseTypeData migrationCaseTypeData, String messageId) {
        var migrationRequest = composeMigrateCaseRequest(migrationCaseData, migrationCaseTypeData);

        MessageLog caseReceived = logNewCase(migrationCaseData, messageId);

        CreateCaseworkCaseResponse caseResponse = createMigrationCase(migrationRequest);

        logCreatedCase(migrationCaseData, messageId, caseReceived);

        log.info("Created migration case {}", caseResponse.getUuid());
    }

    private CreateCaseworkCaseResponse createMigrationCase(CreateMigrationCaseRequest migrationRequest) {
        CreateCaseworkCaseResponse caseResponse = null;
        try {
            caseResponse = migrationCaseworkClient.migrateCase(migrationRequest);

        } catch (Exception e) {
            // Log failure to migration_failures table here.
            log.info("Could not create case, reason: " + e.getMessage());
        }
        return caseResponse;
    }

    private MessageLog logNewCase(MigrationData migrationCaseData, String messageId) {
        MessageLog caseReceived = new MessageLog(
                UUID.fromString(messageId),
                messageId,
                UUID.randomUUID(),  // need to get from message
                migrationCaseData.getRawPayload(),
                Status.NEW,
                null,
                LocalDateTime.now()
        );
        migrationStateService.createState(caseReceived);
        return caseReceived;
    }

    private void logCreatedCase(MigrationData migrationCaseData, String messageId, MessageLog originalCaseReceived) {
        MessageLog caseCreated = new MessageLog(
                UUID.fromString(messageId),
                messageId,
                UUID.randomUUID(),  // need to get from message
                migrationCaseData.getRawPayload(),
                Status.COMPLETED,
                LocalDateTime.now(),
                originalCaseReceived.getReceived()
        );
        migrationStateService.createState(caseCreated);
    }

    private CreateMigrationCaseRequest composeMigrateCaseRequest(MigrationData migrationData, MigrationCaseTypeData migrationCaseTypeData) {
        Map<String, String> initialData = Map.of(CHANNEL_LABEL, migrationCaseTypeData.getOrigin());

        MigrationComplaintCorrespondent primaryCorrespondent = getPrimaryCorrespondent(migrationData.getPrimaryCorrespondent());
        List<MigrationComplaintCorrespondent> additionalCorrespondents = getAdditionalCorrespondents(migrationData.getAdditionalCorrespondents());
        List<CaseAttachment> caseAttachments = getCaseAttachments(migrationData.getCaseAttachments());

        return new CreateMigrationCaseRequest(migrationData.getComplaintType(),
                migrationData.getDateReceived(),
                initialData,
                "MIGRATION",
                primaryCorrespondent,
                additionalCorrespondents,
                caseAttachments);
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

    public void createState() {
        migrationStateService.createTestCase();
    }
}
