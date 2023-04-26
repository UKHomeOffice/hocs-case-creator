package uk.gov.digital.ho.hocs.client.migration.workflow;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseResponse;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCorrespondentRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.MigrationComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.migration.workflow.dto.CreateWorkflowRequest;
import uk.gov.digital.ho.hocs.domain.queue.complaints.CorrespondentType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MigrationWorkflowClientTest {

    private final String serviceUrl = "http://localhost:8091";
    private MigrationWorkflowClient migrationWoirkflowClient;
    @Mock
    private RestClient restClient;

    @Before
    public void setUp() {
        migrationWoirkflowClient = new MigrationWorkflowClient(restClient, serviceUrl);
    }

    @Test
    public void shouldCreateWorkflow() {

        UUID caseUUID = UUID.randomUUID();


        CreateWorkflowRequest request = new CreateWorkflowRequest(caseUUID);

        ResponseEntity responseEntity = new ResponseEntity<>(HttpStatus.OK);

        when(restClient.post(serviceUrl, "/migrate/case", request, void.class)).thenReturn(responseEntity);

        migrationWoirkflowClient.createWorkflow(request);

        verify(restClient).post(serviceUrl, "/migrate/case", request, void.class);
    }

}
