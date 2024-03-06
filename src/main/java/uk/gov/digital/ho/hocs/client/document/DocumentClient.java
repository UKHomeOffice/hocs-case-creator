package uk.gov.digital.ho.hocs.client.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.document.dto.CreateDocumentRequest;

import java.util.UUID;

@Slf4j
@Component
@Profile("migration")
public class DocumentClient {

    private final RestClient restClient;

    private final String serviceBaseURL;

    public DocumentClient(RestClient restClient, @Value("${case.creator.document-service}") String documentService) {
        this.restClient = restClient;
        this.serviceBaseURL = documentService;
    }

    public ResponseEntity<UUID> createDocument(String messageId, CreateDocumentRequest request) {
        ResponseEntity<UUID> response = restClient.post(messageId, serviceBaseURL, "/document", request, UUID.class);
        return response;
    }
}
