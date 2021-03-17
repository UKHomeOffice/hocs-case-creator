package uk.gov.digital.ho.hocs.client.workflow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.application.RestHelper;
import uk.gov.digital.ho.hocs.client.workflow.dto.AdvanceCaseDataRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.queue.UKVIComplaintService.CASE_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowClientTest {

    private final String serviceUrl = "http://localhost:8091";
    private WorkflowClient workflowClient;
    @Mock
    private RestHelper restHelper;

    @Before
    public void setUp() {
        workflowClient = new WorkflowClient(restHelper, serviceUrl);
    }

    @Test
    public void createCase() {
        UUID responseUUID = UUID.randomUUID();
        String caseRef = "COMP/0120003/21";
        CreateCaseRequest request = new CreateCaseRequest(CASE_TYPE, LocalDate.of(2021, 1, 1));

        CreateCaseResponse expectedResponse = new CreateCaseResponse(responseUUID, caseRef);
        ResponseEntity<CreateCaseResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restHelper.post(serviceUrl, "/case", request, CreateCaseResponse.class)).thenReturn(responseEntity);

        CreateCaseResponse actualResponse = workflowClient.createCase(request);

        assertEquals(expectedResponse.getUuid(), actualResponse.getUuid());
        assertEquals(expectedResponse.getReference(), actualResponse.getReference());
    }

    @Test
    public void shouldAdvanceCase() {
        UUID caseUUID = UUID.randomUUID();
        UUID currentStageUUID = UUID.randomUUID();
        Map<String, String> data = Map.of("ComplaintType", "BIOMETRIC_RESIDENCE_PERMIT");

        String jsonFromService = "{\n" +
                "  \"stageUUID\" : \"%s\"\n" +
                "}\n";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(String.format(jsonFromService, currentStageUUID), HttpStatus.OK);
        AdvanceCaseDataRequest request = new AdvanceCaseDataRequest(data);

        when(restHelper.post(serviceUrl, String.format("/case/%s/stage/%s", caseUUID, currentStageUUID), request, String.class)).thenReturn(responseEntity);

        UUID actualStageUUID = workflowClient.advanceCase(caseUUID, currentStageUUID, data);

        assertEquals(currentStageUUID, actualStageUUID);

    }
}