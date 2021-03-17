package uk.gov.digital.ho.hocs.client.workflow;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.AdvanceCaseDataRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class WorkflowClient {
    private final RestClient restClient;
    private final String serviceBaseURL;

    @Autowired
    public WorkflowClient(RestClient restClient, @Value("${hocs.workflow-service}") String serviceBaseURL) {
        this.restClient = restClient;
        this.serviceBaseURL = serviceBaseURL;
    }

    public CreateCaseResponse createCase(CreateCaseRequest request) {
        ResponseEntity<CreateCaseResponse> responseEntity = restClient.post(serviceBaseURL, "/case", request, CreateCaseResponse.class);
        return responseEntity.getBody();
    }

    public UUID advanceCase(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        AdvanceCaseDataRequest request = new AdvanceCaseDataRequest(data);
        ResponseEntity<String> responseEntity = restClient.post(serviceBaseURL, String.format("/case/%s/stage/%s", caseUUID, stageUUID), request, String.class);
        return UUID.fromString(JsonPath.read(responseEntity.getBody(), "$.stageUUID"));
    }
}
