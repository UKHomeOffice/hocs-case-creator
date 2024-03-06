package uk.gov.digital.ho.hocs.client.migration.workflow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.migration.workflow.dto.CreateWorkflowRequest;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MigrationWorkflowClientTest {

    private final String serviceUrl = "http://localhost:8091";
    private MigrationWorkflowClient migrationWoirkflowClient;
    private String messageId;
    @Mock
    private RestClient restClient;

    @Before
    public void setUp() {
        migrationWoirkflowClient = new MigrationWorkflowClient(restClient, serviceUrl);
        messageId = UUID.randomUUID().toString();
    }

    @Test
    public void shouldCreateWorkflow() {
        UUID caseUUID = UUID.randomUUID();

        CreateWorkflowRequest request = new CreateWorkflowRequest(caseUUID);

        ResponseEntity responseEntity = new ResponseEntity<>(HttpStatus.OK);

        when(restClient.post(messageId, serviceUrl, "/migrate/case", request, void.class)).thenReturn(responseEntity);

        migrationWoirkflowClient.createWorkflow(messageId, request);

        verify(restClient).post(messageId, serviceUrl, "/migrate/case", request, void.class);
    }

}
