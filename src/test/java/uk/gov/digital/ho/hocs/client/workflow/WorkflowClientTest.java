package uk.gov.digital.ho.hocs.client.workflow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.AdvanceCaseDataRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.client.workflow.dto.DocumentSummary;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.queue.complaints.ComplaintService.DOCUMENT_TYPE;
import static uk.gov.digital.ho.hocs.queue.complaints.ComplaintService.ORIGINAL_FILENAME;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowClientTest {

    private final String serviceUrl = "http://localhost:8091";
    private WorkflowClient workflowClient;
    @Mock
    private RestClient restClient;

    @Before
    public void setUp() {
        workflowClient = new WorkflowClient(restClient, serviceUrl);
    }

    @Test
    public void createCase() {
        UUID responseUUID = UUID.randomUUID();
        String caseRef = "COMP/0120003/21";
        String s3ObjectName = "8bdc5724-80e4-4fe3-a0a9-1f00262107b0";
        String caseType = "COMP";
        DocumentSummary documentSummary = new DocumentSummary(ORIGINAL_FILENAME, DOCUMENT_TYPE, s3ObjectName);
        CreateCaseRequest request = new CreateCaseRequest(caseType, LocalDate.of(2021, 1, 1), List.of(documentSummary), Map.of());

        CreateCaseResponse expectedResponse = new CreateCaseResponse(responseUUID, caseRef);
        ResponseEntity<CreateCaseResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restClient.post(serviceUrl, "/case", request, CreateCaseResponse.class)).thenReturn(responseEntity);

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

        when(restClient.post(serviceUrl, String.format("/case/%s/stage/%s", caseUUID, currentStageUUID), request, String.class)).thenReturn(responseEntity);

        UUID actualStageUUID = workflowClient.advanceCase(caseUUID, currentStageUUID, data);

        assertEquals(currentStageUUID, actualStageUUID);

    }
}
