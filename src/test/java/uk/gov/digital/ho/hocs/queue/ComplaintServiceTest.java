package uk.gov.digital.ho.hocs.queue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.digital.ho.hocs.client.ComplaintData;
import uk.gov.digital.ho.hocs.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.client.casework.dto.StageAndUserResponse;
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
    @Value("${hocs.user}")
    private String user;

    @Before
    public void setUp() {
        complaintService = new ComplaintService(workflowClient, caseworkClient, user);
    }

    @Test
    public void shouldCreateComplaint() throws Exception {
        String json = getResourceFileAsString("staffBehaviour.json");

        LocalDate receivedDate = LocalDate.parse("2020-10-03");
        String decsReference = "COMP/01";
        UUID userUUID = UUID.randomUUID();
        UUID caseUUID = UUID.randomUUID();
        UUID stageForCaseUUID = UUID.randomUUID();
        UUID primaryCorrespondent = UUID.randomUUID();
        CreateCaseRequest createCaseRequest = new CreateCaseRequest(UKVIComplaintService.CASE_TYPE, receivedDate);
        CreateCaseResponse createCaseResponse = new CreateCaseResponse(caseUUID, decsReference);

        StageAndUserResponse stageAndUserResponse = new StageAndUserResponse(stageForCaseUUID, userUUID);

        when(workflowClient.createCase(createCaseRequest)).thenReturn(createCaseResponse);

        when(caseworkClient.getStageAndUserForCase(caseUUID)).thenReturn(stageAndUserResponse);

        when(caseworkClient.getPrimaryCorrespondent(caseUUID)).thenReturn(primaryCorrespondent);

        complaintService.createComplaint(new ComplaintData(json), UKVIComplaintService.CASE_TYPE);

        verify(caseworkClient).updateStageUser(caseUUID, stageForCaseUUID, userUUID);

        verify(caseworkClient).addCorrespondentToCase(eq(caseUUID), eq(stageForCaseUUID), any(UKVIComplaintCorrespondent.class));

        verify(workflowClient, times(2)).advanceCase(eq(caseUUID), eq(stageForCaseUUID), anyMap());

    }

}