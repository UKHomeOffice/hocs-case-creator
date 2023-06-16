package uk.gov.digital.ho.hocs.client.migration.casework;

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
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreatePrimaryTopicRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.MigrationComplaintCorrespondent;
import uk.gov.digital.ho.hocs.domain.queue.complaints.CorrespondentType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MigrationCaseworkClientTest {

    private final String serviceUrl = "http://localhost:8082";
    private String messageId;
    private MigrationCaseworkClient migrationCaseworkClient;
    @Mock
    private RestClient restClient;

    @Before
    public void setUp() {
        migrationCaseworkClient = new MigrationCaseworkClient(restClient, serviceUrl);
        messageId = UUID.randomUUID().toString();
    }

    @Test
    public void shouldMigrateCase() {
        UUID responseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseRef = "COMP/0120003/21";
        Map<String, String> data = new HashMap<>();
        LocalDate date = LocalDate.now();

        CreateMigrationCaseRequest request = new CreateMigrationCaseRequest("Migration", date, date, date, date, data, "COMP_MIGRATION_END", "test");

        CreateMigrationCaseResponse expectedResponse = new CreateMigrationCaseResponse(responseUUID, stageUUID, caseRef, data);
        ResponseEntity<CreateMigrationCaseResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restClient.post(messageId, serviceUrl, "/migrate/case", request, CreateMigrationCaseResponse.class)).thenReturn(responseEntity);

        migrationCaseworkClient.migrateCase(messageId, request);

        verify(restClient).post(messageId, serviceUrl, "/migrate/case", request, CreateMigrationCaseResponse.class);
    }

    @Test
    public void shouldMigrateCorrespondent() {
        CreateMigrationCorrespondentRequest request = new CreateMigrationCorrespondentRequest(UUID.randomUUID(), UUID.randomUUID(), createCorrespondent(), List.of(createCorrespondent()));

        migrationCaseworkClient.migrateCorrespondent(messageId, request);

        verify(restClient).post(messageId, serviceUrl, "/migrate/correspondent", request, Void.class);
    }

    @Test
    public void shouldCreatePrimaryTopic() {
        CreatePrimaryTopicRequest request = new CreatePrimaryTopicRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        migrationCaseworkClient.createPrimaryTopic(messageId, request);

        verify(restClient).post(messageId, serviceUrl, "/migrate/primary-topic", request, Void.class);
    }

    private MigrationComplaintCorrespondent createCorrespondent() {
        return new MigrationComplaintCorrespondent("fullName", CorrespondentType.COMPLAINANT, "address1", "address2", "address3", "postcode", "country", "organisation", "telephone", "email", "reference");
    }
}
