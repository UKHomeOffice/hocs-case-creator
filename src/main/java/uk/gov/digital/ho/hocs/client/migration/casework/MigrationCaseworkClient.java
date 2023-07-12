package uk.gov.digital.ho.hocs.client.migration.casework;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.casework.dto.UpdateMigratedCaseDataRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseResponse;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCorrespondentRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreatePrimaryTopicRequest;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class MigrationCaseworkClient {
    private final RestClient restClient;
    private final String serviceBaseURL;

    public MigrationCaseworkClient(RestClient restClient, @Value("${case.creator.case-service}") String serviceBaseURL) {
        this.restClient = restClient;
        this.serviceBaseURL = serviceBaseURL;
    }

    public CreateMigrationCaseResponse migrateCase(String messageId, CreateMigrationCaseRequest request) {
        ResponseEntity<CreateMigrationCaseResponse> responseEntity = restClient.post(
                messageId,
                serviceBaseURL,
                "/migrate/case",
                request,
                CreateMigrationCaseResponse.class);
        return responseEntity.getBody();
    }

    public ResponseEntity<Void> migrateCorrespondent(String messageId, CreateMigrationCorrespondentRequest request) {
        return restClient.post(
                messageId,
                serviceBaseURL,
                "/migrate/correspondent",
                request,
                Void.class);
    }

    public void createPrimaryTopic(String messageId, CreatePrimaryTopicRequest request) {
        restClient.post(
            messageId,
            serviceBaseURL,
            "/migrate/primary-topic",
            request,
            Void.class
        );
    }

    public void updateMigratedCaseData(
        String messageId,
        String migratedCaseReference,
        LocalDateTime updateTimestamp,
        Map<String, String> data
    ) {
        UpdateMigratedCaseDataRequest request = new UpdateMigratedCaseDataRequest(updateTimestamp, data);
        restClient.post(
            messageId,
            serviceBaseURL,
            String.format("/migrate/case/%s/case-data", migratedCaseReference),
            request,
            Void.class
        );
    }

}
