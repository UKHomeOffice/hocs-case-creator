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
import uk.gov.digital.ho.hocs.client.workflow.dto.DocumentSummary;

import java.util.LinkedHashMap;
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
        // dummy documents list
        DocumentSummary documentSummary = new DocumentSummary("migration","","");
        var migrationRequest = composeMigrateCaseRequest(migrationCaseData, migrationCaseTypeData, documentSummary);
        CreateCaseworkCaseResponse caseResponse = migrationCaseworkClient.migrateCase(migrationRequest);
        log.info("Created migration case {}", caseResponse.getUuid());
    }

    private CreateMigrationCaseRequest composeMigrateCaseRequest(MigrationData migrationData, MigrationCaseTypeData migrationCaseTypeData, DocumentSummary documentSummary) {
        Map<String, String> initialData = Map.of(CHANNEL_LABEL, migrationCaseTypeData.getOrigin());

        MigrationComplaintCorrespondent primaryCorrespondent = getPrimaryCorrespondent(migrationData.getPrimaryCorrespondent());
        List<MigrationComplaintCorrespondent> additionalCorrespondents = getAdditionalCorrespondents(migrationData.getAdditionalCorrespondents());

        return new CreateMigrationCaseRequest(migrationData.getComplaintType(),
                migrationData.getDateReceived(),
                List.of(documentSummary),
                initialData,
                "MIGRATION",
                primaryCorrespondent,
                additionalCorrespondents);
    }

    public MigrationComplaintCorrespondent getPrimaryCorrespondent(LinkedHashMap correspondentJson) {
        MigrationComplaintCorrespondent primaryCorrespondent = objectMapper.convertValue(
                correspondentJson,
                new TypeReference<>() {
                });
        return primaryCorrespondent;
    }

    public List<MigrationComplaintCorrespondent> getAdditionalCorrespondents(JSONArray correspondentJson) {
        List<MigrationComplaintCorrespondent> additionalCorrespondents = objectMapper.convertValue(
                correspondentJson,
                new TypeReference<>() {
                });
        return additionalCorrespondents;
    }
}
