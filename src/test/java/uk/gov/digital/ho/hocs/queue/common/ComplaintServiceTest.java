package uk.gov.digital.ho.hocs.queue.common;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.client.audit.AuditClient;
import uk.gov.digital.ho.hocs.client.audit.dto.EventType;
import uk.gov.digital.ho.hocs.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.document.DocumentS3Client;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.client.workflow.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.queue.common.ComplaintService;
import uk.gov.digital.ho.hocs.queue.common.ComplaintTypeData;
import uk.gov.digital.ho.hocs.queue.ukvi.UKVIComplaintData;
import uk.gov.digital.ho.hocs.queue.ukvi.UKVITypeData;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.queue.common.ComplaintService.DOCUMENT_TYPE;
import static uk.gov.digital.ho.hocs.queue.common.ComplaintService.ORIGINAL_FILENAME;
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
    private AuditClient auditClient;
    @Mock
    private DocumentS3Client documentS3Client;

    private ComplaintService complaintService;

    private String user;
    private UUID stageForCaseUUID;
    private UUID primaryCorrespondent;
    private CreateCaseRequest createCaseRequest;
    private CreateCaseResponse createCaseResponse;
    private String json;
    private String expectedText;
    private final String s3ObjectName = "8bdc5724-80e4-4fe3-a0a9-1f00262107b0";
    private UUID caseUUID;
    private ComplaintTypeData complaintTypeData;

    @Before
    public void setUp() {
        json = getResourceFileAsString("staffBehaviour.json");
        expectedText = getResourceFileAsString("staffBehaviour.txt");

        LocalDate receivedDate = LocalDate.parse("2020-10-03");
        String decsReference = "COMP/01";
        caseUUID = UUID.randomUUID();
        stageForCaseUUID = UUID.randomUUID();
        primaryCorrespondent = UUID.randomUUID();
        complaintTypeData = new UKVITypeData();
        DocumentSummary documentSummary = new DocumentSummary(ORIGINAL_FILENAME, DOCUMENT_TYPE, s3ObjectName);
        createCaseRequest = new CreateCaseRequest(complaintTypeData.getCaseType(), receivedDate, List.of(documentSummary));
        createCaseResponse = new CreateCaseResponse(caseUUID, decsReference);
        user = UUID.randomUUID().toString();
        when(clientContext.getUserId()).thenReturn(user);
        complaintService = new ComplaintService(workflowClient, caseworkClient, clientContext, auditClient, documentS3Client);
    }

    @Test
    public void shouldCreateComplaint() throws IOException {

        goodSetup();

        complaintService.createComplaint(new UKVIComplaintData(json), complaintTypeData);

        verify(caseworkClient).updateStageUser(caseUUID, stageForCaseUUID, UUID.fromString(user));

        verify(caseworkClient).addCorrespondentToCase(eq(caseUUID), eq(stageForCaseUUID), any(ComplaintCorrespondent.class));

        verify(workflowClient, times(2)).advanceCase(eq(caseUUID), eq(stageForCaseUUID), anyMap());

        verify(auditClient).audit(EventType.CREATOR_CASE_CREATED, caseUUID, stageForCaseUUID, json);

        verify(auditClient).audit(eq(EventType.CREATOR_CORRESPONDENT_CREATED), eq(caseUUID), eq(stageForCaseUUID), anyMap());
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
        complaintService.createComplaint(new UKVIComplaintData(json), complaintTypeData);
    }
    
    @Test(expected = NullPointerException.class)
    public void createCaseShouldThrowException() {
        goodSetup();
        when(workflowClient.createCase(createCaseRequest)).thenThrow(new NullPointerException());
        complaintService.createComplaint(new UKVIComplaintData(json), complaintTypeData);
    }

    @Test
    public void getStageForCaseShouldCatchException() {
        goodSetup();
        when(caseworkClient.getStageForCase(caseUUID)).thenThrow(new NullPointerException());
        complaintService.createComplaint(new UKVIComplaintData(json), complaintTypeData);
    }
    
    @Test
    public void auditShouldCatchException() throws IOException {
        goodSetup();
        doThrow(IOException.class).when(auditClient).audit(EventType.CREATOR_CASE_CREATED, caseUUID, stageForCaseUUID, json);
        complaintService.createComplaint(new UKVIComplaintData(json), complaintTypeData);
    }
    
    @Test
    public void updateStageUserShouldCatchException() {
        goodSetup();
        when(caseworkClient.updateStageUser(caseUUID, stageForCaseUUID, UUID.fromString(user))).thenThrow(new NullPointerException());
        complaintService.createComplaint(new UKVIComplaintData(json), complaintTypeData);
    }
    
    @Test
    public void addCorrespondentToCaseShouldCatchException() {
        goodSetup();
        when(caseworkClient.addCorrespondentToCase(eq(caseUUID), eq(stageForCaseUUID), any(ComplaintCorrespondent.class))).thenThrow(new NullPointerException());
        complaintService.createComplaint(new UKVIComplaintData(json), complaintTypeData);
    }
    
    @Test
    public void getPrimaryCorrespondentShouldCatchException() {
        goodSetup();
        when(caseworkClient.getPrimaryCorrespondent(caseUUID)).thenThrow(new NullPointerException());
        complaintService.createComplaint(new UKVIComplaintData(json), complaintTypeData);
    }
    
    @Test
    public void advanceCaseShouldCatchException() {
        goodSetup();
        when(workflowClient.advanceCase(eq(caseUUID), eq(stageForCaseUUID), anyMap())).thenThrow(new NullPointerException());
        complaintService.createComplaint(new UKVIComplaintData(json), complaintTypeData);
    }
    
}
