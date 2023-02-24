package uk.gov.digital.ho.hocs.domain.queue.complaints;

import com.amazonaws.AmazonServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.application.MessageContext;
import uk.gov.digital.ho.hocs.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.document.DocumentS3Client;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.client.workflow.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.queue.complaints.ukvi.UKVIComplaintData;
import uk.gov.digital.ho.hocs.domain.queue.complaints.ukvi.UKVITypeData;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.domain.queue.complaints.ComplaintService.DOCUMENT_TYPE;
import static uk.gov.digital.ho.hocs.domain.queue.complaints.ComplaintService.ORIGINAL_FILENAME;
import static uk.gov.digital.ho.hocs.utilities.TestFileReader.getResourceFileAsString;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"local", "ukvi"})
public class ComplaintServiceTest {

    @MockBean
    private WorkflowClient workflowClient;
    @MockBean
    private CaseworkClient caseworkClient;
    @SpyBean
    private MessageContext messageContext;
    @MockBean
    private DocumentS3Client documentS3Client;
    @SpyBean
    private ObjectMapper objectMapper;
    @MockBean
    private EnumMappingsRepository enumMappingsRepository;
    @MockBean
    private MessageLogService messageLogService;

    private ComplaintService complaintService;

    private UUID stageForCaseUUID;
    private CreateCaseRequest createCaseRequest;
    private CreateCaseResponse createCaseResponse;
    private String json;
    private String expectedText;
    private UUID caseUUID;
    private ComplaintTypeData complaintTypeData;

    @Before
    public void setUp() {
        json = getResourceFileAsString("webform/staffBehaviour.json");
        expectedText = getResourceFileAsString("webform/staffBehaviour.txt");

        messageContext.initialiseContext(UUID.randomUUID().toString());

        String s3ObjectName = UUID.randomUUID().toString();
        caseUUID = UUID.randomUUID();
        stageForCaseUUID = UUID.randomUUID();
        UUID primaryCorrespondent = UUID.randomUUID();
        complaintTypeData = new UKVITypeData();

        var initialCaseData = Map.of(
                "ComplaintType", "POOR_INFORMATION_OR_STAFF_BEHAVIOUR",
                "Channel", "Webform",
                "XOriginatedFrom", "Webform");

        DocumentSummary documentSummary = new DocumentSummary(ORIGINAL_FILENAME, DOCUMENT_TYPE, s3ObjectName);
        createCaseRequest = new CreateCaseRequest(complaintTypeData.getCaseType(), LocalDate.parse("2020-10-03"), List.of(documentSummary), initialCaseData);
        createCaseResponse = new CreateCaseResponse(caseUUID, "TEST/01");
        complaintService = new ComplaintService(workflowClient, caseworkClient, messageContext, documentS3Client, messageLogService);

        // Happy path minimum
        when(documentS3Client.storeUntrustedDocument(ORIGINAL_FILENAME, expectedText)).thenReturn(s3ObjectName);
        when(workflowClient.createCase(createCaseRequest)).thenReturn(createCaseResponse);
        when(caseworkClient.getStageForCase(caseUUID)).thenReturn(stageForCaseUUID);
        when(caseworkClient.getPrimaryCorrespondent(caseUUID)).thenReturn(primaryCorrespondent);
    }

    @Test
    public void shouldCreateComplaint() {
        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);

        // Document addition
        verify(documentS3Client).storeUntrustedDocument(ORIGINAL_FILENAME, expectedText);
        verify(messageLogService).updateStatus(messageContext.getCorrelationId(), Status.CASE_DOCUMENT_CREATED);
        // Case creation
        verify(workflowClient).createCase(createCaseRequest);
        verify(messageLogService).updateCaseUuidAndStatus(messageContext.getCorrelationId(), caseUUID, Status.CASE_CREATED);
        // Get stage
        verify(caseworkClient).getStageForCase(caseUUID);
        verify(messageLogService).updateStatus(messageContext.getCorrelationId(), Status.CASE_STAGE_RETRIEVED);
        // Update user
        verify(caseworkClient).updateStageUser(caseUUID, stageForCaseUUID, UUID.fromString(messageContext.getUserId()));
        verify(messageLogService).updateStatus(messageContext.getCorrelationId(), Status.CASE_USER_UPDATED);
        // Correspondent addition
        verify(caseworkClient, times(2)).addCorrespondentToCase(eq(caseUUID), eq(stageForCaseUUID), any(ComplaintCorrespondent.class));
        verify(caseworkClient).getPrimaryCorrespondent(caseUUID);
        verify(caseworkClient).updateCase(eq(caseUUID), eq(stageForCaseUUID), anyMap());
        verify(messageLogService).updateStatus(messageContext.getCorrelationId(), Status.CASE_CORRESPONDENTS_HANDLED);
        // Update team
        verify(caseworkClient).updateStageTeam(caseUUID, stageForCaseUUID, UUID.fromString(messageContext.getTeamId()));
        verify(messageLogService).updateStatus(messageContext.getCorrelationId(), Status.CASE_TEAM_UPDATED);
    }

    @Test(expected = ApplicationExceptions.DocumentCreationException.class)
    public void shouldThrowAndUpdateMessageLogWhenDocumentUploadThrowsException() {
        when(documentS3Client.storeUntrustedDocument(ORIGINAL_FILENAME, expectedText)).thenThrow(new AmazonServiceException("Test"));

        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);

        verify(messageLogService).updateStatus(messageContext.getCorrelationId(), Status.CASE_DOCUMENT_FAILED);
    }

    @Test(expected = ApplicationExceptions.CaseCreationException.class)
    public void shouldThrowAndUpdateMessageLogWhenCaseCreationThrowsException() {
        when(workflowClient.createCase(createCaseRequest)).thenReturn(createCaseResponse);

        when(workflowClient.createCase(createCaseRequest)).thenThrow(new RuntimeException("Test"));

        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);

        verify(messageLogService).updateStatus(messageContext.getCorrelationId(), Status.CASE_CREATION_FAILED);
    }

    @Test(expected = ApplicationExceptions.CaseStageRetrievalException.class)
    public void shouldThrowAndUpdateMessageLogWhenGetStageThrowsException() {
        when(caseworkClient.getStageForCase(caseUUID)).thenThrow(new RuntimeException("Test"));

        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);

        verify(messageLogService).updateStatus(messageContext.getCorrelationId(), Status.CASE_STAGE_RETRIEVAL_FAILED);
    }

    @Test(expected = ApplicationExceptions.CaseUserUpdateException.class)
    public void shouldThrowAndUpdateMessageLogWhenUpdateUserThrowsException() {
        when(caseworkClient.updateStageUser(caseUUID, stageForCaseUUID, UUID.fromString(messageContext.getUserId()))).thenThrow(new RuntimeException("Test"));

        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);

        verify(messageLogService).updateStatus(messageContext.getCorrelationId(), Status.CASE_USER_UPDATE_FAILED);
    }

    @Test(expected = ApplicationExceptions.CaseCorrespondentCreationException.class)
    public void shouldThrowAndUpdateMessageLogAddingCorrespondentThrowsException() {
        when(caseworkClient.addCorrespondentToCase(eq(caseUUID), eq(stageForCaseUUID), any(ComplaintCorrespondent.class))).thenThrow(new RuntimeException("Test"));

        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);

        verify(messageLogService).updateStatus(messageContext.getCorrelationId(), Status.CASE_CORRESPONDENTS_FAILED);
    }

    @Test(expected = ApplicationExceptions.CaseCorrespondentCreationException.class)
    public void shouldThrowAndUpdateMessageLogGettingPrimaryCorrespondentThrowsException() {
        when(caseworkClient.getPrimaryCorrespondent(caseUUID)).thenThrow(new RuntimeException("Test"));

        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);

        verify(messageLogService).updateStatus(messageContext.getCorrelationId(), Status.CASE_CORRESPONDENTS_FAILED);
    }

    @Test(expected = ApplicationExceptions.CaseCorrespondentCreationException.class)
    public void shouldThrowAndUpdateMessageLogUpdatingPrimaryCorrespondentThrowsException() {
        when(caseworkClient.updateCase(eq(caseUUID), eq(stageForCaseUUID), anyMap())).thenThrow(new RuntimeException("Test"));

        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);

        verify(messageLogService).updateStatus(messageContext.getCorrelationId(), Status.CASE_CORRESPONDENTS_FAILED);
    }

    @Test(expected = ApplicationExceptions.CaseTeamUpdateException.class)
    public void shouldThrowAndUpdateMessageLogWhenUpdateTeamThrowsException() {
        when(caseworkClient.updateStageTeam(caseUUID, stageForCaseUUID, UUID.fromString(messageContext.getTeamId()))).thenThrow(new RuntimeException("Test"));

        complaintService.createComplaint(new UKVIComplaintData(json, objectMapper, enumMappingsRepository), complaintTypeData);

        verify(messageLogService).updateStatus(messageContext.getCorrelationId(), Status.CASE_TEAM_UPDATE_FAILED);
    }

}
