package uk.gov.digital.ho.hocs.queue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.client.audit.AuditClient;
import uk.gov.digital.ho.hocs.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.queue.common.ComplaintService;
import uk.gov.digital.ho.hocs.queue.common.ComplaintTypeData;
import uk.gov.digital.ho.hocs.queue.ukvi.UKVIComplaintData;
import uk.gov.digital.ho.hocs.queue.ukvi.UKVITypeData;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
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

    private ComplaintService complaintService;

    private String user;

    @Before
    public void setUp() {
        user = UUID.randomUUID().toString();
        when(clientContext.getUserId()).thenReturn(user);
        complaintService = new ComplaintService(workflowClient, caseworkClient, clientContext, auditClient);
    }

    @Test
    public void shouldCreateComplaint() {
        String json = getResourceFileAsString("staffBehaviour.json");

        LocalDate receivedDate = LocalDate.parse("2020-10-03");
        String decsReference = "COMP/01";
        UUID caseUUID = UUID.randomUUID();
        UUID stageForCaseUUID = UUID.randomUUID();
        UUID primaryCorrespondent = UUID.randomUUID();
        ComplaintTypeData complaintTypeData = new UKVITypeData();
        CreateCaseRequest createCaseRequest = new CreateCaseRequest(complaintTypeData.getCaseType(), receivedDate);
        CreateCaseResponse createCaseResponse = new CreateCaseResponse(caseUUID, decsReference);

        when(workflowClient.createCase(createCaseRequest)).thenReturn(createCaseResponse);

        when(caseworkClient.getStageForCase(caseUUID)).thenReturn(stageForCaseUUID);

        when(caseworkClient.getPrimaryCorrespondent(caseUUID)).thenReturn(primaryCorrespondent);

        complaintService.createComplaint(new UKVIComplaintData(json), complaintTypeData);

        verify(caseworkClient).updateStageUser(caseUUID, stageForCaseUUID, UUID.fromString(user));

        verify(caseworkClient).addCorrespondentToCase(eq(caseUUID), eq(stageForCaseUUID), any(ComplaintCorrespondent.class));

        verify(workflowClient, times(2)).advanceCase(eq(caseUUID), eq(stageForCaseUUID), anyMap());

    }

}