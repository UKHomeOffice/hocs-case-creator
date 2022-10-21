package uk.gov.digital.ho.hocs.queue.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.PathNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.application.ClientContext;

import uk.gov.digital.ho.hocs.client.casework.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.client.migration.casework.MigrationCaseworkClient;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.client.document.DocumentS3Client;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.MigrationComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.queue.complaints.CorrespondentType;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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

    private MigrationService migrationService;

    private CreateMigrationCaseRequest createMigrationCaseRequest;

    private CreateCaseworkCaseResponse caseworkCaseResponse;

    private MigrationData migrationData;

    private String json;

    private MigrationCaseTypeData migrationCaseTypeData;

    private DocumentSummary documentSummary;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        json = getResourceFileAsString("validMigration.json");
        migrationData = new MigrationData(json);
        migrationCaseTypeData = new MigrationCaseTypeData();
        Map<String, String> initialData = Map.of("Channel", migrationCaseTypeData.getOrigin());
        objectMapper = new ObjectMapper();
        migrationService = new MigrationService(workflowClient, migrationCaseworkClient, clientContext, documentS3Client, objectMapper);

        MigrationComplaintCorrespondent primaryCorrespondent = migrationService.getPrimaryCorrespondent(
                migrationData.getPrimaryCorrespondent());

        documentSummary = new DocumentSummary("migration","","");
        createMigrationCaseRequest = new CreateMigrationCaseRequest(migrationData.getComplaintType(),
                migrationData.getDateReceived(), List.of(documentSummary),
                initialData,
                "MIGRATION",
                primaryCorrespondent);
        caseworkCaseResponse = new CreateCaseworkCaseResponse();
        when(migrationCaseworkClient.migrateCase(any(CreateMigrationCaseRequest.class))).thenReturn(caseworkCaseResponse);
    }

    @Test
    public void migrateCase() {
        migrationService.createMigrationCase(migrationData, migrationCaseTypeData);
        verify(migrationCaseworkClient, times(1)).migrateCase(createMigrationCaseRequest);
    }

    @Test
    public void shouldContainAPrimaryCorrespondent() {
        MigrationComplaintCorrespondent primaryCorrespondents = createMigrationCaseRequest.getPrimaryCorrespondent();

        MigrationComplaintCorrespondent expectedPrimaryCorrespondent =
                new MigrationComplaintCorrespondent(
                        "fullName",
                        CorrespondentType.COMPLAINANT,
                        "address1",
                        "address2",
                        "address3",
                        "postcode",
                        "country",
                        "organisation",
                        "telephone",
                        "email",
                        "reference"
                        );
        assertEquals(expectedPrimaryCorrespondent, primaryCorrespondents);
    }

    @Test(expected= PathNotFoundException.class)
    public void shouldFailWithMissingPrimaryCorrespondent(){
        json = getResourceFileAsString("invalidMigrationMissingPrimaryCorrespondent.json");
        migrationData = new MigrationData(json);
        migrationCaseTypeData = new MigrationCaseTypeData();
        migrationService = new MigrationService(workflowClient, migrationCaseworkClient, clientContext, documentS3Client, objectMapper);

        migrationData.getPrimaryCorrespondent();
    }
}