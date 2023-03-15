package uk.gov.digital.ho.hocs.client.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.document.dto.CreateDocumentRequest;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.application.LogEvent.DOCUMENT_CLIENT_CREATE_SUCCESS;
import static uk.gov.digital.ho.hocs.application.LogEvent.EVENT;

@Slf4j
@Component
public class DocumentClient {

    private final RestClient restClient;

    private final String serviceBaseURL;

    @Autowired
    public DocumentClient(RestClient restClient, @Value("${case.creator.document-service}") String documentService) {
        this.restClient = restClient;
        this.serviceBaseURL = documentService;
    }

    public ResponseEntity<UUID> createDocument(UUID caseUUID, CreateDocumentRequest request) {
        ResponseEntity<UUID> response = restClient.post(serviceBaseURL, "/document", request, UUID.class);
        log.info("Created Document {}, Case {}", response.getBody(), caseUUID, value(EVENT, DOCUMENT_CLIENT_CREATE_SUCCESS));
        return response;
    }
}