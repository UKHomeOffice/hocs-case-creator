package uk.gov.digital.ho.hocs.domain.queue.migration.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.client.document.DocumentClient;
import uk.gov.digital.ho.hocs.client.document.dto.CreateDocumentRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.*;
import uk.gov.digital.ho.hocs.domain.queue.complaints.CorrespondentType;
import uk.gov.digital.ho.hocs.domain.queue.migration.CaseAttachment;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static uk.gov.digital.ho.hocs.domain.queue.migration.MigrationService.CHANNEL_LABEL;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "local", "integration" })
public class MigrationServiceIntegrationTest {

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @Autowired
    private  DocumentClient documentClient;

    @Autowired
    private RestTemplate restTemplate;

    int port = 8082;

    @Test
    public void createMigratedClosedCase() {
        CreateMigrationCaseRequest createCaseRequest = getCreateCaseRequest();
        ResponseEntity<CreateMigrationCaseResponse> caseResponse = getCreateCaseResponse(createCaseRequest);

        assertThat(caseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(caseResponse.getBody().getReference()).isNotNull();
        assertThat(caseResponse.getBody().getUuid()).isNotNull();
        assertThat(caseResponse.getBody().getStageId()).isNotNull();
        assertThat(caseResponse.getBody().getReference()).isNotNull();
        assertThat(caseResponse.getBody().getData()).isNotNull();
    }

    @Test
    public void createMigratedClosedCaseWithCorrespondents() {
        CreateMigrationCaseRequest createCaseRequest = getCreateCaseRequest();
        ResponseEntity<CreateMigrationCaseResponse> caseResponse = getCreateCaseResponse(createCaseRequest);

        CreateMigrationCorrespondentRequest createCorrespondentRequest =
                getCreateCorrespondentRequest(
                        caseResponse.getBody().getUuid(),
                        caseResponse.getBody().getStageId());
        ResponseEntity correspondentResponse = getCreateCorrespondentResponse(createCorrespondentRequest);

        assertThat(caseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(caseResponse.getBody().getReference()).isNotNull();
        assertThat(caseResponse.getBody().getUuid()).isNotNull();
        assertThat(caseResponse.getBody().getStageId()).isNotNull();
        assertThat(caseResponse.getBody().getReference()).isNotNull();
        assertThat(caseResponse.getBody().getData()).isNotNull();

        assertThat(correspondentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void createMigratedClosedCaseWithCorrespondentsAndCaseAttachments() {
        CreateMigrationCaseRequest createCaseRequest = getCreateCaseRequest();
        ResponseEntity<CreateMigrationCaseResponse> caseResponse = getCreateCaseResponse(createCaseRequest);

        UUID caseId = caseResponse.getBody().getUuid();

        CreateMigrationCorrespondentRequest createCorrespondentRequest =
                getCreateCorrespondentRequest(
                        caseId,
                        caseResponse.getBody().getStageId());
        ResponseEntity correspondentResponse = getCreateCorrespondentResponse(createCorrespondentRequest);

        assertThat(caseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(caseResponse.getBody().getReference()).isNotNull();
        assertThat(caseResponse.getBody().getUuid()).isNotNull();
        assertThat(caseResponse.getBody().getStageId()).isNotNull();
        assertThat(caseResponse.getBody().getReference()).isNotNull();
        assertThat(caseResponse.getBody().getData()).isNotNull();

        assertThat(correspondentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        CreateDocumentRequest createCaseAttachmentRequest = getCreateAttachmentRequest(caseId);
        ResponseEntity<UUID> caseAttachmentResponse = getCreateCaseAttachmentResponse(createCaseAttachmentRequest);

        assertThat(caseAttachmentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(caseAttachmentResponse.getBody()).isNotNull();
        assertThat(caseAttachmentResponse.getBody()).isInstanceOf(UUID.class);
    }

    private CreateDocumentRequest getCreateAttachmentRequest(UUID caseId) {
        CaseAttachment caseAttachment = createCaseAttachment();

        return new CreateDocumentRequest(
                caseAttachment.getDisplayName(),
                caseAttachment.getType(),
                caseAttachment.getDocumentPath(),
                caseId
        );
    }

    private CaseAttachment createCaseAttachment() {
        return new CaseAttachment(
                "2a4e860b-78dd-4a97-9ac2-b849fff9d949.pdf",
                "To document",
                "a3ef5b0c-2ab0-4354-9191-028ca4ee1ec5c"
        );
    }

    private CreateMigrationCorrespondentRequest getCreateCorrespondentRequest(UUID caseId, UUID stageId) {
        return new CreateMigrationCorrespondentRequest(
                caseId,
                stageId,
                createCorrespondent(),
                List.of(createCorrespondent(), createCorrespondent()));
    }

    private ResponseEntity<CreateMigrationCaseResponse> getCreateCaseResponse(CreateMigrationCaseRequest request) {
        return testRestTemplate.exchange(
                getBasePath() + "/migrate/case",
                POST,
                new HttpEntity(request, createValidAuthHeaders()),
                CreateMigrationCaseResponse.class);
    }

    private ResponseEntity getCreateCorrespondentResponse(CreateMigrationCorrespondentRequest request) {
        return testRestTemplate.exchange(
                getBasePath() + "/migrate/correspondent",
                POST,
                new HttpEntity(request, createValidAuthHeaders()),
                ResponseEntity.class);
    }

    private ResponseEntity<UUID> getCreateCaseAttachmentResponse(CreateDocumentRequest request) {
        return testRestTemplate.exchange(
                getCaseAttachmentBasePath() + "/document",
                POST,
                new HttpEntity(request, createValidAuthHeaders()),
                UUID.class);
    }

    private CreateMigrationCaseRequest getCreateCaseRequest() {
        return new CreateMigrationCaseRequest(
                "COMP",
                LocalDate.now(),
                Map.of(CHANNEL_LABEL,"CMS"),
                "MIGRATION");

    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private String getCaseAttachmentBasePath() {
        return "http://localhost:" + 8083;
    }

    private HttpHeaders createValidAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/RERERCIiIiIiIiIiIiIiIg");
        headers.add("X-Auth-Userid", "9f69d980-258b-448c-a958-199a698dfd42");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }

    private MigrationComplaintCorrespondent createCorrespondent() {
        return new MigrationComplaintCorrespondent(
                "fullName",
                CorrespondentType.COMPLAINANT,
                "address1",
                "address2",
                "address3",
                "postcode",
                "country",
                "organisation",
                "telephone",
                "email",
                "reference"
        );
    }
}