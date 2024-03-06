package uk.gov.digital.ho.hocs.client.migration.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.migration.workflow.dto.CreateWorkflowRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;

@Component
public class MigrationWorkflowClient {
    private final RestClient restClient;
    private final String serviceBaseURL;

    @Autowired
    public MigrationWorkflowClient(RestClient restClient, @Value("${case.creator.workflow-service}") String serviceBaseURL) {
        this.restClient = restClient;
        this.serviceBaseURL = serviceBaseURL;
    }

    public ResponseEntity createWorkflow(String messageId, CreateWorkflowRequest request) {
        return restClient.post(messageId, serviceBaseURL, "/migrate/case", request, void.class);
    }

}
