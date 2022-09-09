package uk.gov.digital.ho.hocs.client.migration.casework;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.casework.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.client.casework.dto.UpdateStageTeamRequest;
import uk.gov.digital.ho.hocs.client.casework.dto.UpdateStageUserRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.queue.complaints.CorrespondentType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MigrationCaseworkClientTest {

    private final String serviceUrl = "http://localhost:8082";
    private MigrationCaseworkClient migrationCaseworkClient;
    @Mock
    private RestClient restClient;

    @Before
    public void setUp() {
        migrationCaseworkClient = new MigrationCaseworkClient(restClient, serviceUrl);
    }

    @Test
    public void shouldMigrateCase() {
        UUID responseUUID = UUID.randomUUID();
        String caseRef = "COMP/0120003/21";
        Map<String, String> data = new HashMap<>();

        CreateMigrationCaseRequest request = new CreateMigrationCaseRequest(null, null, null, null, null);
        CreateCaseworkCaseResponse expectedResponse = new CreateCaseworkCaseResponse(responseUUID, caseRef, data);
        ResponseEntity<CreateCaseworkCaseResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restClient.post(serviceUrl, "/migrate", request, CreateCaseworkCaseResponse.class)).thenReturn(responseEntity);

        migrationCaseworkClient.migrateCase(request);

        verify(restClient).post(serviceUrl, "/migrate", request, CreateCaseworkCaseResponse.class);
    }
}
