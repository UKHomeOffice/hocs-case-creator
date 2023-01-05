package uk.gov.digital.ho.hocs.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class RestClient {

    private final RestTemplate restTemplate;
    private final ClientContext clientContext;

    @Autowired
    public RestClient(RestTemplate restTemplate,
                      ClientContext clientContext) {
        this.restTemplate = restTemplate;
        this.clientContext = clientContext;
    }

    public <T, R> ResponseEntity<R> post(String serviceBaseURL, String url, T request, Class<R> responseType) {
        log.info("RestClient making POST request to {}{}", serviceBaseURL, url);
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.POST, new HttpEntity<>(request, createAuthHeaders(clientContext)), responseType);
    }

    public <T, R> ResponseEntity<R> put(String serviceBaseURL, String url, T request, Class<R> responseType) {
        log.info("RestClient making PUT request to {}{}", serviceBaseURL, url);
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.PUT, new HttpEntity<>(request, createAuthHeaders(clientContext)), responseType);
    }

    public <T, R> ResponseEntity<R> get(String serviceBaseURL, String url, Class<R> responseType) {
        log.info("RestClient making GET request to {}{}", serviceBaseURL, url);
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders(clientContext)), responseType);
    }

    HttpHeaders createAuthHeaders(ClientContext clientContext) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(ClientContext.CORRELATION_ID_HEADER, clientContext.getCorrelationId());
        headers.add(ClientContext.GROUP_HEADER, clientContext.getGroups());
        headers.add(ClientContext.USER_ID_HEADER, clientContext.getUserId());
        return headers;
    }

}
