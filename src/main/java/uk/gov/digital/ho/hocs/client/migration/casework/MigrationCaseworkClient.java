package uk.gov.digital.ho.hocs.client.migration.casework;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseResponse;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCorrespondentRequest;

@Slf4j
@Component
public class MigrationCaseworkClient {
    private final RestClient restClient;
    private final String serviceBaseURL;

    public MigrationCaseworkClient(RestClient restClient, @Value("${case.creator.case-service}") String serviceBaseURL) {
        this.restClient = restClient;
        this.serviceBaseURL = serviceBaseURL;
    }

    public ResponseEntity migrateCase(CreateMigrationCaseRequest request) {
        ResponseEntity<CreateMigrationCaseResponse> responseEntity = restClient.post(
                serviceBaseURL,
                "/migrate/case",
                request,
                CreateMigrationCaseResponse.class);
        return responseEntity;
    }

    public ResponseEntity migrateCorrespondent(CreateMigrationCorrespondentRequest request) {
        ResponseEntity responseEntity = restClient.post(
                serviceBaseURL,
                "/migrate/correspondent",
                request,
                Void.class);
        return responseEntity;
    }
}