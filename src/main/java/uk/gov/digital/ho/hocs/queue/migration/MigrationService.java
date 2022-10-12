package uk.gov.digital.ho.hocs.queue.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.client.casework.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.client.document.DocumentS3Client;
import uk.gov.digital.ho.hocs.client.migration.casework.MigrationCaseworkClient;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MigrationService {

    public static final String CHANNEL_LABEL = "Channel";

    private final WorkflowClient workflowClient;
    private final MigrationCaseworkClient migrationCaseworkClient;
    private final ClientContext clientContext;
    private final DocumentS3Client documentS3Client;
    private final ObjectMapper objectMapper;

    public MigrationService(WorkflowClient workflowClient,
                            MigrationCaseworkClient migrationCaseworkClient,
                            ClientContext clientContext,
                            DocumentS3Client documentS3Client,
                            ObjectMapper objectMapper) {
        this.workflowClient = workflowClient;
        this.migrationCaseworkClient = migrationCaseworkClient;
        this.clientContext = clientContext;
        this.documentS3Client = documentS3Client;
        this.objectMapper = objectMapper;
    }

    public void createMigrationCase(MigrationData migrationCaseData, MigrationCaseTypeData migrationCaseTypeData) {
        List<CaseAttachment> caseAttachments = getCaseAttachments(migrationCaseData.getCaseAttachments());
        var migrationRequest = composeMigrateCaseRequest(migrationCaseData, migrationCaseTypeData, caseAttachments);
        CreateCaseworkCaseResponse caseResponse = migrationCaseworkClient.migrateCase(migrationRequest);
        log.info("Created migration case {}", caseResponse.getUuid());
    }

    private List<CaseAttachment> getCaseAttachments(String attachments) {
        CaseAttachment[] caseAttachments;
        try {
            caseAttachments = objectMapper.readValue(attachments, CaseAttachment[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return Arrays.asList(caseAttachments);
    }

    private CreateMigrationCaseRequest composeMigrateCaseRequest(MigrationData migrationData, MigrationCaseTypeData migrationCaseTypeData, List<CaseAttachment> caseAttachments) {
        Map<String, String> initialData = Map.of(CHANNEL_LABEL, migrationCaseTypeData.getOrigin());
        return new CreateMigrationCaseRequest(migrationData.getComplaintType(), migrationData.getDateReceived(), caseAttachments, initialData, "MIGRATION");
    }

}
