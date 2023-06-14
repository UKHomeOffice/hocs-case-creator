package uk.gov.digital.ho.hocs.domain.queue.migration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.PathNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.client.document.DocumentClient;
import uk.gov.digital.ho.hocs.client.document.dto.CreateDocumentRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.MigrationCaseworkClient;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.*;
import uk.gov.digital.ho.hocs.client.migration.workflow.MigrationWorkflowClient;
import uk.gov.digital.ho.hocs.client.migration.workflow.dto.CreateWorkflowRequest;
import uk.gov.digital.ho.hocs.domain.queue.complaints.CorrespondentType;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

import java.time.LocalDate;
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
    private MessageLogService messageLogService;

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private TopicMapper topicMapper;

    private MigrationService migrationService;

    private CreateMigrationCaseRequest createMigrationCaseRequest;

    private CreateMigrationCorrespondentRequest createMigrationCorrespondentRequest;

    private MigrationData migrationData;

    private String json;

    private ObjectMapper objectMapper;

    private String messageId;

    @Before
    public void setUp() {
        Map<String, String> initialData = Map.of();

        json = getResourceFileAsString("migration/validMigration.json");
        migrationData = new MigrationData(json);
        objectMapper = new ObjectMapper();
        migrationService = new MigrationService(
                migrationCaseworkClient,
                objectMapper,
                messageLogService,
                documentClient,
                workflowClient,
                caseDataService,
                topicMapper
        );
        messageId = UUID.randomUUID().toString();
        UUID topicId = UUID.randomUUID();

        MigrationComplaintCorrespondent primaryCorrespondent = migrationService.getPrimaryCorrespondent(
                migrationData.getPrimaryCorrespondent());

        List<MigrationComplaintCorrespondent> additionalCorrespondents = migrationService.getAdditionalCorrespondents(
                messageId,
                UUID.randomUUID(),
                migrationData.getAdditionalCorrespondents());

        createMigrationCaseRequest = new CreateMigrationCaseRequest(
                migrationData.getComplaintType(),
                migrationData.getDateCreated(),
                migrationData.getDateReceived(),
                migrationData.getCaseDeadline(),
                migrationData.getDateCompleted(),
                initialData,
                StageTypeMapping.getStageType("COMP"),
                migrationData.getMigratedReference());

        CreateMigrationCaseResponse caseworkCaseResponse = new CreateMigrationCaseResponse(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "reference",
            Collections.emptyMap()
        );

        when(migrationCaseworkClient.migrateCase(eq(messageId), any(CreateMigrationCaseRequest.class))).thenReturn(
            caseworkCaseResponse);

        createMigrationCorrespondentRequest = new CreateMigrationCorrespondentRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                primaryCorrespondent,
                additionalCorrespondents);

        ResponseEntity<Void> correspondentResponseEntity = new ResponseEntity<>(
                null,
                null,
                HttpStatus.OK
        );

        when(migrationCaseworkClient.migrateCorrespondent(eq(messageId), any(CreateMigrationCorrespondentRequest.class))).thenReturn(correspondentResponseEntity);

        ResponseEntity<UUID> caseAttachmentResponseEntity = new ResponseEntity<>(
                UUID.randomUUID(),
                null,
                HttpStatus.OK
        );

        when(documentClient.createDocument(any(), any(CreateDocumentRequest.class))).thenReturn(caseAttachmentResponseEntity);

        when(workflowClient.createWorkflow(eq(messageId), any(CreateWorkflowRequest.class))).thenReturn(ResponseEntity.ok().build());

        when(topicMapper.getTopicId(messageId, "Primary topic")).thenReturn(Optional.of(topicId));
    }

    @Test
    public void migrateOpenCaseWithWorkflow() {
        var json = getResourceFileAsString("migration/validMigrationOpenCOMP.json");
        var migrationData = new MigrationData(json);

        migrationService.createMigrationCase(messageId, migrationData);

        verify(migrationCaseworkClient, times(1)).migrateCase(eq(messageId), any());
        verify(migrationCaseworkClient, times(1)).migrateCorrespondent(eq(messageId), any());
        verify(workflowClient, times(1)).createWorkflow(eq(messageId), any());

        verify(topicMapper, never()).getTopicId(any(), any());
        verify(migrationCaseworkClient, never()).createPrimaryTopic(any(), any());
    }

    @Test
    public void migrateClosedCase() {
        migrationService.createMigrationCase(messageId, migrationData);
        verify(migrationCaseworkClient, times(1)).migrateCase(messageId, createMigrationCaseRequest);
        verify(migrationCaseworkClient, times(1)).migrateCorrespondent(eq(messageId), any());
        verify(documentClient, times(1)).createDocument(any(), any());

        verify(workflowClient, never()).createWorkflow(eq(messageId), any());
        verify(topicMapper, never()).getTopicId(any(), any());
        verify(migrationCaseworkClient, never()).createPrimaryTopic(any(), any());
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
        migrationService = new MigrationService(
            migrationCaseworkClient,
            objectMapper,
            messageLogService,
            documentClient,
            workflowClient,
            caseDataService,
            topicMapper
        );

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
        migrationService = new MigrationService(
            migrationCaseworkClient,
            objectMapper,
            messageLogService,
            documentClient,
            workflowClient,
            caseDataService,
            topicMapper
        );

        List<MigrationComplaintCorrespondent> migrationComplaintCorrespondents =
                migrationService.getAdditionalCorrespondents(
                        messageId,
                        UUID.randomUUID(),
                        migrationData.getAdditionalCorrespondents());

        assertTrue(migrationComplaintCorrespondents.isEmpty());
    }

    @Test
    public void shouldNotContainAttachments() {
        json = getResourceFileAsString("migration/validMigrationNoCaseAttachments.json");
        migrationData = new MigrationData(json);
        migrationService = new MigrationService(
            migrationCaseworkClient,
            objectMapper,
            messageLogService,
            documentClient,
            workflowClient,
            caseDataService,
            topicMapper
        );

        List<CaseAttachment> caseAttachments =
                migrationService.getCaseAttachments(
                        messageId,
                        UUID.randomUUID(),
                        migrationData.getCaseAttachments());
        assertTrue(caseAttachments.isEmpty());
    }

    @Test
    public void shouldContainCorrectCaseTypeOnParse() {
        String messageId = UUID.randomUUID().toString();
        json = getResourceFileAsString("migration/validMigrationCOMP.json");
        migrationData = new MigrationData(json);

        CreateMigrationCaseRequest request =
                migrationService.composeMigrateCaseRequest(messageId, migrationData);

        assertEquals("COMP", request.getType());
    }

    @Test
    public void shouldNotSendCompletedDateForOpenCases() {
        String messageId = UUID.randomUUID().toString();
        json = getResourceFileAsString("migration/validMigrationOpenCOMP.json");
        migrationData = new MigrationData(json);

        CreateMigrationCaseRequest request =
            migrationService.composeMigrateCaseRequest(messageId, migrationData);

        assertNull(request.getDateCompleted());
    }

    @Test
    public void shouldReadDatesFromMessageJson() {
        String messageId = UUID.randomUUID().toString();
        json = getResourceFileAsString("migration/validMigrationCOMP.json");
        migrationData = new MigrationData(json);

        CreateMigrationCaseRequest request =
            migrationService.composeMigrateCaseRequest(messageId, migrationData);

        assertEquals(request.getDateCreated(), LocalDate.parse("2010-06-22"));
        assertEquals(request.getDateReceived(), LocalDate.parse("1965-06-21"));
        assertEquals(request.getCaseDeadline(), LocalDate.parse("1965-07-21"));
        assertEquals(request.getDateCompleted(), LocalDate.parse("2022-09-01"));
    }

    @Test
    public void shouldLogConflictResponsesAsDuplicateCases() {
        when(migrationCaseworkClient.migrateCase(eq(messageId), any())).thenThrow(HttpClientErrorException.create(
            HttpStatus.CONFLICT,
            "Conflict",
            HttpHeaders.EMPTY,
            null,
            null
        ));

        var json = getResourceFileAsString("migration/validMigrationOpenCOMP.json");
        var migrationData = new MigrationData(json);

        Status status = migrationService.createMigrationCase(messageId, migrationData);

        verify(migrationCaseworkClient, times(1)).migrateCase(eq(messageId), any());
        verify(migrationCaseworkClient, times(0)).migrateCorrespondent(eq(messageId), any());
        verify(documentClient, times(0)).createDocument(eq(messageId), any());
        verify(workflowClient, times(0)).createWorkflow(eq(messageId), any());

        assertEquals(Status.DUPLICATE_MIGRATED_REFERENCE, status);
    }

    @Test
    public void itAppendsTheChannelToProvidedCaseData() {
        var json = getResourceFileAsString("migration/validMigrationOpenCOMP.json");
        var migrationData = new MigrationData(json);

        Map<String, String> caseData = new HashMap<>(Map.of(
            "name1", "value1",
            "name2", "value2"
        ));

        when(caseDataService.parseCaseDataJson(messageId, migrationData.getCaseDataJson()))
            .thenReturn(caseData);

        migrationService.createMigrationCase(messageId, migrationData);
    }

    @Test
    public void whenAPrimaryTopicIsIncluded_theTopicShouldBeAddedToTheCase() {
        var json = getResourceFileAsString("migration/validMigrationTOWithPrimaryTopic.json");
        var migrationData = new MigrationData(json);

        migrationService.createMigrationCase(messageId, migrationData);

        verify(topicMapper, times(1)).getTopicId(messageId, "Primary topic");
        verify(migrationCaseworkClient, times(1))
                .createPrimaryTopic(eq(messageId), any(CreatePrimaryTopicRequest.class));
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
