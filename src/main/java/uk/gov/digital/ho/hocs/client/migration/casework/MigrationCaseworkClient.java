package uk.gov.digital.ho.hocs.client.migration.casework;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.casework.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseRequest;

@Slf4j
@Component
public class MigrationCaseworkClient {
    private final RestClient restClient;
    private final String serviceBaseURL;

    public MigrationCaseworkClient(RestClient restClient, @Value("${case.creator.case-service}") String serviceBaseURL) {
        this.restClient = restClient;
        this.serviceBaseURL = serviceBaseURL;
    }

    public CreateCaseworkCaseResponse migrateCase(CreateMigrationCaseRequest request) {
        ResponseEntity<CreateCaseworkCaseResponse> responseEntity = restClient.post(serviceBaseURL, "/migrate", request, CreateCaseworkCaseResponse.class);
        return responseEntity.getBody();
    }

}
