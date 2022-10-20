package uk.gov.digital.ho.hocs.queue.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.client.casework.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.client.document.DocumentS3Client;
import uk.gov.digital.ho.hocs.client.migration.casework.MigrationCaseworkClient;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

@RunWith(MockitoJUnitRunner.class)
public class MigrationServiceTest {

    @Mock
    private WorkflowClient workflowClient;

    @Mock
    private MigrationCaseworkClient migrationCaseworkClient;

    @Mock
    private ClientContext clientContext;

    @Mock
    private DocumentS3Client documentS3Client;

    private ObjectMapper objectMapper;

    private MigrationService migrationService;

    private CreateMigrationCaseRequest createMigrationCaseRequest;

    private CreateCaseworkCaseResponse caseworkCaseResponse;

    private MigrationData migrationData;

    private String json;

    private MigrationCaseTypeData migrationCaseTypeData;

    private List<CaseAttachment> caseAttachment;

    @Before
    public void setUp() {
        json = getResourceFileAsString("validMigration.json");
        migrationData = new MigrationData(json);
        objectMapper = new ObjectMapper();
        migrationCaseTypeData = new MigrationCaseTypeData();
        Map<String, String> initialData = Map.of("Channel", migrationCaseTypeData.getOrigin());
        caseAttachment = new ArrayList<>();
        caseAttachment.add(new CaseAttachment("document1.pdf","To document","e7f5d229-3f23-450c-8f11-8ef647943ae3"));
        caseAttachment.add(new CaseAttachment("document2.pdf","pdf","9bf2665f-6b21-47af-8789-34a25b136670"));
        createMigrationCaseRequest = new CreateMigrationCaseRequest(migrationData.getComplaintType(), migrationData.getDateReceived(), caseAttachment, initialData, "MIGRATION");
        caseworkCaseResponse = new CreateCaseworkCaseResponse();
        migrationService = new MigrationService(workflowClient, migrationCaseworkClient, clientContext, documentS3Client, objectMapper);
        when(migrationCaseworkClient.migrateCase(any(CreateMigrationCaseRequest.class))).thenReturn(caseworkCaseResponse);
    }

    @Test
    public void migrateCase() {
        migrationService.createMigrationCase(migrationData, migrationCaseTypeData);
        verify(migrationCaseworkClient, times(1)).migrateCase(createMigrationCaseRequest);
    }
}
