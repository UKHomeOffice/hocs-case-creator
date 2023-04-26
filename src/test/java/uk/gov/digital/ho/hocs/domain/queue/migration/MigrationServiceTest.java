package uk.gov.digital.ho.hocs.domain.queue.migration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.PathNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.test.context.ActiveProfiles;
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
import uk.gov.digital.ho.hocs.domain.queue.complaints.CorrespondentType;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.utilities.TestFileReader.getResourceFileAsString;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("migration")
public class MigrationServiceTest {

    @Mock
    private MigrationCaseworkClient migrationCaseworkClient;

    @Mock
    private DocumentClient documentClient;
    @Mock
    private MigrationWorkflowClient workflowClient;
    @Mock
    private RequestData requestData;
    @Mock
    private MessageLogService messageLogService;

    private MigrationService migrationService;

    private CreateMigrationCaseRequest createMigrationCaseRequest;

    private CreateMigrationCaseResponse caseworkCaseResponse;

    private CreateMigrationCorrespondentRequest createMigrationCorrespondentRequest;

    private CreateDocumentRequest createMigrationCaseAttachmentRequest;

    private MigrationData migrationData;

    private String json;

    private MigrationCaseTypeData migrationCaseTypeData;

    private List<CaseAttachment> caseAttachment;

    private ObjectMapper objectMapper;

    @Mock
    private ResponseEntity<CreateMigrationCaseResponse> responseEntity;

    @Before
    public void setUp() {
        json = getResourceFileAsString("migration/validMigration.json");
        migrationData = new MigrationData(json);
        migrationCaseTypeData = new MigrationCaseTypeData();
        Map<String, String> initialData = Map.of("Channel", migrationCaseTypeData.getOrigin());
        objectMapper = new ObjectMapper();
        migrationService = new MigrationService(
                migrationCaseworkClient,
                requestData,
                objectMapper,
                messageLogService,
                documentClient,
                workflowClient);

        MigrationComplaintCorrespondent primaryCorrespondent = migrationService.getPrimaryCorrespondent(
                migrationData.getPrimaryCorrespondent());

        List<MigrationComplaintCorrespondent> additionalCorrespondents = migrationService.getAdditionalCorrespondents(
                migrationData.getAdditionalCorrespondents());

        caseAttachment = new ArrayList<>();
        caseAttachment.add(new CaseAttachment("document1.pdf","To document","e7f5d229-3f23-450c-8f11-8ef647943ae3"));
        caseAttachment.add(new CaseAttachment("document2.pdf","pdf","9bf2665f-6b21-47af-8789-34a25b136670"));

        createMigrationCaseRequest = new CreateMigrationCaseRequest(
                migrationData.getComplaintType(),
                migrationData.getDateReceived(),
                migrationData.getDateCompleted(),
                initialData,
                StageTypeMapping.getStageType("COMP"));

        caseworkCaseResponse = new CreateMigrationCaseResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "reference",
                Collections.emptyMap()
        );

        when(migrationCaseworkClient.migrateCase(any(CreateMigrationCaseRequest.class))).thenReturn(caseworkCaseResponse);

        createMigrationCorrespondentRequest = new CreateMigrationCorrespondentRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                primaryCorrespondent,
                additionalCorrespondents);

        ResponseEntity<?> correspondentResponseEntity = new ResponseEntity<>(
                null,
                null,
                HttpStatus.OK
        );

        when(migrationCaseworkClient.migrateCorrespondent(any(CreateMigrationCorrespondentRequest.class))).thenReturn(correspondentResponseEntity);

        createMigrationCaseAttachmentRequest = new CreateDocumentRequest(
                "name",
                "To Document",
                "path",
                UUID.randomUUID()
        );

        ResponseEntity<UUID> caseAttachmentResponseEntity = new ResponseEntity(
                UUID.randomUUID(),
                null,
                HttpStatus.OK
        );

        when(documentClient.createDocument(any(), any(CreateDocumentRequest.class))).thenReturn(caseAttachmentResponseEntity);

        when(workflowClient.createWorkflow(any(CreateWorkflowRequest.class))).thenReturn(ResponseEntity.ok().build());

    }

    @Test
    public void migrateOpenCaseWithWorkflow() {
        var json = getResourceFileAsString("migration/validMigrationOpenCOMP.json");
        var migrationData = new MigrationData(json);

        migrationService.createMigrationCase(migrationData, migrationCaseTypeData);

        verify(migrationCaseworkClient, times(1)).migrateCase(any());
        verify(migrationCaseworkClient, times(1)).migrateCorrespondent(any());
        verify(workflowClient, times(1)).createWorkflow(any());
    }

    @Test
    public void migrateClosedCase() {
        migrationService.createMigrationCase(migrationData, migrationCaseTypeData);
        verify(migrationCaseworkClient, times(1)).migrateCase(createMigrationCaseRequest);
        verify(migrationCaseworkClient, times(1)).migrateCorrespondent(any());
        verify(documentClient, times(1)).createDocument(any(), any());
        verify(workflowClient, never()).createWorkflow(any());
    }

    @Test
    public void shouldContainAPrimaryCorrespondent() {
        MigrationComplaintCorrespondent primaryCorrespondents = createMigrationCorrespondentRequest.getPrimaryCorrespondent();

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
        json = getResourceFileAsString("migration/invalidMigrationMissingPrimaryCorrespondent.json");
        migrationData = new MigrationData(json);
        migrationCaseTypeData = new MigrationCaseTypeData();
        migrationService = new MigrationService(
                migrationCaseworkClient,
                requestData,
                objectMapper,
                messageLogService,
                documentClient, workflowClient);

        migrationData.getPrimaryCorrespondent();
    }

    @Test
    public void shouldContainAdditionalCorrespondents() {
        List<MigrationComplaintCorrespondent> additionalCorrespondents = createMigrationCorrespondentRequest.getAdditionalCorrespondents();

        List<MigrationComplaintCorrespondent> expectedAdditionalCorrespondents = new ArrayList<>();
        expectedAdditionalCorrespondents.add(createCorrespondent());
        expectedAdditionalCorrespondents.add(createCorrespondent());

        assertEquals(expectedAdditionalCorrespondents, additionalCorrespondents);
    }

    @Test
    public void shouldContainNoAdditionalCorrespondents() {
        json = getResourceFileAsString("migration/validMigrationNoAdditionalCorrespondents.json");
        migrationData = new MigrationData(json);
        migrationCaseTypeData = new MigrationCaseTypeData();
        migrationService = new MigrationService(
                migrationCaseworkClient,
                requestData,
                objectMapper,
                messageLogService,
                documentClient, workflowClient);

        List<MigrationComplaintCorrespondent> migrationComplaintCorrespondents =
                migrationService.getAdditionalCorrespondents(
                        migrationData.getAdditionalCorrespondents());

        assertTrue(migrationComplaintCorrespondents.isEmpty());
    }

    @Test
    public void shouldNotContainAttachments() {
        json = getResourceFileAsString("migration/validMigrationNoCaseAttachments.json");
        migrationData = new MigrationData(json);
        migrationCaseTypeData = new MigrationCaseTypeData();
        migrationService = new MigrationService(
                migrationCaseworkClient,
                requestData,
                objectMapper,
                messageLogService,
                documentClient, workflowClient);

        List<CaseAttachment> caseAttachments =
                migrationService.getCaseAttachments(UUID.randomUUID(),
                        migrationData.getCaseAttachments());
        assertTrue(caseAttachments.isEmpty());
    }

    @Test
    public void shouldContainCorrectCaseTypeOnParse() {
        json = getResourceFileAsString("migration/validMigrationCOMP.json");
        migrationData = new MigrationData(json);

        MigrationCaseTypeData migrationCaseTypeData = new MigrationCaseTypeData();
        migrationCaseTypeData.setCaseType(migrationData.getComplaintType());

        CreateMigrationCaseRequest request =
                migrationService.composeMigrateCaseRequest(migrationData, migrationCaseTypeData);

        assertEquals("COMP", request.getType());
    }

    @Test
    public void shouldNotSendCompletedDateForOpenCases() {
        json = getResourceFileAsString("migration/validMigrationOpenCOMP.json");
        migrationData = new MigrationData(json);

        MigrationCaseTypeData migrationCaseTypeData = new MigrationCaseTypeData();
        migrationCaseTypeData.setCaseType(migrationData.getComplaintType());

        CreateMigrationCaseRequest request =
            migrationService.composeMigrateCaseRequest(migrationData, migrationCaseTypeData);

        assertNull(request.getDateCompleted());
    }

    private MigrationComplaintCorrespondent createCorrespondent() {
        return new MigrationComplaintCorrespondent(
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
    }
}
