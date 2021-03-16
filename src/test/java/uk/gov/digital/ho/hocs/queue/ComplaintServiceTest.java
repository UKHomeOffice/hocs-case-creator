package uk.gov.digital.ho.hocs.queue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.client.ComplaintData;
import uk.gov.digital.ho.hocs.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.client.casework.dto.UKVIComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

@RunWith(MockitoJUnitRunner.class)
public class ComplaintServiceTest {

    @Mock
    WorkflowClient workflowClient;
    @Mock
    CaseworkClient caseworkClient;

    ComplaintService complaintService;
    private String user;

    @Before
    public void setUp() {
        user = UUID.randomUUID().toString();
        complaintService = new ComplaintService(workflowClient, caseworkClient, user);
    }

    @Test
    public void shouldCreateComplaint() throws Exception {
        String json = getResourceFileAsString("staffBehaviour.json");

        LocalDate receivedDate = LocalDate.parse("2020-10-03");
        String decsReference = "COMP/01";
        UUID caseUUID = UUID.randomUUID();
        UUID stageForCaseUUID = UUID.randomUUID();
        UUID primaryCorrespondent = UUID.randomUUID();
        CreateCaseRequest createCaseRequest = new CreateCaseRequest(UKVIComplaintService.CASE_TYPE, receivedDate);
        CreateCaseResponse createCaseResponse = new CreateCaseResponse(caseUUID, decsReference);

        when(workflowClient.createCase(createCaseRequest)).thenReturn(createCaseResponse);

        when(caseworkClient.getStageForCase(caseUUID)).thenReturn(stageForCaseUUID);

        when(caseworkClient.getPrimaryCorrespondent(caseUUID)).thenReturn(primaryCorrespondent);

        complaintService.createComplaint(new ComplaintData(json), UKVIComplaintService.CASE_TYPE);

        verify(caseworkClient).updateStageUser(caseUUID, stageForCaseUUID, UUID.fromString(user));

        verify(caseworkClient).addCorrespondentToCase(eq(caseUUID), eq(stageForCaseUUID), any(UKVIComplaintCorrespondent.class));

        verify(workflowClient, times(2)).advanceCase(eq(caseUUID), eq(stageForCaseUUID), anyMap());

    }

}