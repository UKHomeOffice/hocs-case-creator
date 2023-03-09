package uk.gov.digital.ho.hocs.queue.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.PathNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.application.ClientContext;

import uk.gov.digital.ho.hocs.client.document.DocumentClient;
import uk.gov.digital.ho.hocs.client.migration.casework.MigrationCaseworkClient;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseResponse;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCorrespondentRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.MigrationComplaintCorrespondent;
import uk.gov.digital.ho.hocs.queue.complaints.CorrespondentType;
import uk.gov.digital.ho.hocs.service.MessageLogService;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

@RunWith(MockitoJUnitRunner.class)
public class MigrationServiceTest {


    @Mock
    private MigrationCaseworkClient migrationCaseworkClient;

    @Mock
    private DocumentClient documentClient;
    @Mock
    private ClientContext clientContext;
    @Mock
    private MessageLogService messageLogService;

    private MigrationService migrationService;

    private CreateMigrationCaseRequest createMigrationCaseRequest;

    private CreateMigrationCaseResponse caseworkCaseResponse;

    private CreateMigrationCorrespondentRequest createMigrationCorrespondentRequest;

    private MigrationData migrationData;

    private String json;

    private MigrationCaseTypeData migrationCaseTypeData;

    private List<CaseAttachment> caseAttachment;

    private ObjectMapper objectMapper;

    @Mock
    private ResponseEntity<CreateMigrationCaseResponse> responseEntity;

    @Before
    public void setUp() {
        json = getResourceFileAsString("validMigration.json");
        migrationData = new MigrationData(json);
        migrationCaseTypeData = new MigrationCaseTypeData();
        Map<String, String> initialData = Map.of("Channel", migrationCaseTypeData.getOrigin());
        objectMapper = new ObjectMapper();
        migrationService = new MigrationService(
                migrationCaseworkClient,
                clientContext,
                objectMapper,
                messageLogService,
                documentClient);

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
                initialData,
                "MIGRATION");
        caseworkCaseResponse = new CreateMigrationCaseResponse();

        ResponseEntity<?> responseEntity = new ResponseEntity<>(
                caseworkCaseResponse,
                null,
                HttpStatus.OK
        );

        when(migrationCaseworkClient.migrateCase(any(CreateMigrationCaseRequest.class))).thenReturn(responseEntity);

        createMigrationCorrespondentRequest = new CreateMigrationCorrespondentRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                primaryCorrespondent,
                additionalCorrespondents);

        //Todo: to add case attachment request
    }

    @Test
    public void migrateCase() {
        migrationService.createMigrationCase(migrationData, migrationCaseTypeData);
        verify(migrationCaseworkClient, times(1)).migrateCase(createMigrationCaseRequest);
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
        json = getResourceFileAsString("invalidMigrationMissingPrimaryCorrespondent.json");
        migrationData = new MigrationData(json);
        migrationCaseTypeData = new MigrationCaseTypeData();
        migrationService = new MigrationService(
                migrationCaseworkClient,
                clientContext,
                objectMapper,
                messageLogService,
                documentClient);

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
        json = getResourceFileAsString("validMigrationNoAdditionalCorrespondents.json");
        migrationData = new MigrationData(json);
        migrationCaseTypeData = new MigrationCaseTypeData();
        migrationService = new MigrationService(
                migrationCaseworkClient,
                clientContext,
                objectMapper,
                messageLogService,
                documentClient);

        List<MigrationComplaintCorrespondent> migrationComplaintCorrespondents =
                migrationService.getAdditionalCorrespondents(
                        migrationData.getAdditionalCorrespondents());

        assertTrue(migrationComplaintCorrespondents.isEmpty());
    }

//    @Test
//    public void shouldContainCaseAttachments() {
//        List<CaseAttachment> caseAttachments = createMigrationCaseRequest.getAttachments();
//
//        List<CaseAttachment> expectedAttachments = Arrays.asList(
//                new CaseAttachment(
//                        "document1.pdf",
//                        "To document",
//                        "e7f5d229-3f23-450c-8f11-8ef647943ae3"
//                ),
//                new CaseAttachment(
//                        "document2.pdf",
//                        "pdf",
//                        "9bf2665f-6b21-47af-8789-34a25b136670"
//                ));
//
//        assertEquals(expectedAttachments, caseAttachments);
//    }

    @Test
    public void shouldNotContainAttachments() {
        json = getResourceFileAsString("validMigrationNoCaseAttachments.json");
        migrationData = new MigrationData(json);
        migrationCaseTypeData = new MigrationCaseTypeData();
        migrationService = new MigrationService(
                migrationCaseworkClient,
                clientContext,
                objectMapper,
                messageLogService,
                documentClient);

        List<CaseAttachment> caseAttachments =
                migrationService.getCaseAttachments(
                        migrationData.getCaseAttachments());
        assertTrue(caseAttachments.isEmpty());
    }

    @Test
    public void shouldContainCorrectCaseTypeOnParse() {
        json = getResourceFileAsString("validMigrationCOMP.json");
        migrationData = new MigrationData(json);

        MigrationCaseTypeData migrationCaseTypeData = new MigrationCaseTypeData();
        migrationCaseTypeData.setCaseType(migrationData.getComplaintType());

        CreateMigrationCaseRequest request =
                migrationService.composeMigrateCaseRequest(migrationData, migrationCaseTypeData);

        assertEquals("COMP", request.getType());
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
