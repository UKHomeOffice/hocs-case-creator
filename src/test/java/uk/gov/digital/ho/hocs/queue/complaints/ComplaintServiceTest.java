package uk.gov.digital.ho.hocs.queue.complaints;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.document.DocumentS3Client;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.client.workflow.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;
import uk.gov.digital.ho.hocs.queue.complaints.ukvi.UKVIComplaintData;
import uk.gov.digital.ho.hocs.queue.complaints.ukvi.UKVITypeData;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.queue.complaints.ComplaintService.DOCUMENT_TYPE;
import static uk.gov.digital.ho.hocs.queue.complaints.ComplaintService.ORIGINAL_FILENAME;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

@RunWith(MockitoJUnitRunner.class)
public class ComplaintServiceTest {

    @Mock
    private WorkflowClient workflowClient;
    @Mock
    private CaseworkClient caseworkClient;
    @Mock
    private ClientContext clientContext;
    @Mock
    private DocumentS3Client documentS3Client;
    @SpyBean
    private ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private EnumMappingsRepository enumMappingsRepository;

    private ComplaintService complaintService;

    private String user;
    private String team;
    private UUID stageForCaseUUID;
    private UUID primaryCorrespondent;
    private CreateCaseRequest createCaseRequest;
    private CreateCaseResponse createCaseResponse;
    private String json;
    private String expectedText;
    private String s3ObjectName;
    private UUID caseUUID;
    private ComplaintTypeData complaintTypeData;

    @Before
    public void setUp() {
        json = getResourceFileAsString("staffBehaviour.json");
        expectedText = getResourceFileAsString("staffBehaviour.txt");

        LocalDate receivedDate = LocalDate.parse("2020-10-03");
        String decsReference = "COMP/01";
        s3ObjectName = UUID.randomUUID().toString();
        caseUUID = UUID.randomUUID();
        stageForCaseUUID = UUID.randomUUID();
        primaryCorrespondent = UUID.randomUUID();
        complaintTypeData = new UKVITypeData();
        DocumentSummary documentSummary = new DocumentSummary(ORIGINAL_FILENAME, DOCUMENT_TYPE, s3ObjectName);

        var initialCaseData = Map.of(
                "ComplaintType", "POOR_STAFF_BEHAVIOUR",
                "Channel", "Webform",
                "XOriginatedFrom", "Webform");

        createCaseRequest = new CreateCaseRequest(complaintTypeData.getCaseType(), receivedDate, List.of(documentSummary), initialCaseData);
        createCaseResponse = new CreateCaseResponse(caseUUID, decsReference);
        user = UUID.randomUUID().toString();
        when(clientContext.getUserId()).thenReturn(user);
        team = UUID.randomUUID().toString();
        when(clientContext.getTeamId()).thenReturn(team);
        complaintService = new ComplaintService(workflowClient, caseworkClient, clientContext, documentS3Client);
    }

    @Test
    public void shouldCreateComplaint() throws IOException {
        goodSetup();

        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);

        verify(caseworkClient).updateStageUser(caseUUID, stageForCaseUUID, UUID.fromString(user));

        verify(caseworkClient, times(2)).addCorrespondentToCase(eq(caseUUID), eq(stageForCaseUUID), any(ComplaintCorrespondent.class));

        verify(caseworkClient, times(1)).updateCase(eq(caseUUID), eq(stageForCaseUUID), anyMap());

        verify(caseworkClient).updateStageTeam(caseUUID, stageForCaseUUID, UUID.fromString(team));
    }

    private void goodSetup() {
        when(documentS3Client.storeUntrustedDocument(ORIGINAL_FILENAME, expectedText)).thenReturn(s3ObjectName);

        when(workflowClient.createCase(createCaseRequest)).thenReturn(createCaseResponse);

        when(caseworkClient.getStageForCase(caseUUID)).thenReturn(stageForCaseUUID);

        when(caseworkClient.getPrimaryCorrespondent(caseUUID)).thenReturn(primaryCorrespondent);
    }

    @Test(expected = NullPointerException.class)
    public void storeUntrustedDocumentShouldThrowException() {
        goodSetup();
        when(documentS3Client.storeUntrustedDocument(ORIGINAL_FILENAME, expectedText)).thenThrow(new NullPointerException());
        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);
    }

    @Test(expected = NullPointerException.class)
    public void createCaseShouldThrowException() {
        goodSetup();
        when(workflowClient.createCase(createCaseRequest)).thenThrow(new NullPointerException());
        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);
    }

    @Test
    public void getStageForCaseShouldCatchException() {
        goodSetup();
        when(caseworkClient.getStageForCase(caseUUID)).thenThrow(new NullPointerException());
        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);
    }

    @Test
    public void updateStageUserShouldCatchException() {
        goodSetup();
        when(caseworkClient.updateStageUser(caseUUID, stageForCaseUUID, UUID.fromString(user))).thenThrow(new NullPointerException());
        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);
    }

    @Test
    public void addCorrespondentToCaseShouldCatchException() {
        goodSetup();
        when(caseworkClient.addCorrespondentToCase(eq(caseUUID), eq(stageForCaseUUID), any(ComplaintCorrespondent.class))).thenThrow(new NullPointerException());
        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);
    }

    @Test
    public void getPrimaryCorrespondentShouldCatchException() {
        goodSetup();
        when(caseworkClient.getPrimaryCorrespondent(caseUUID)).thenThrow(new NullPointerException());
        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);
    }
}
