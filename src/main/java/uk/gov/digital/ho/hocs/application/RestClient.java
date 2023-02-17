package uk.gov.digital.ho.hocs.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class RestClient {

    private final RestTemplate restTemplate;

    private final MessageContext messageContext;

    public RestClient(RestTemplate restTemplate,
                      MessageContext messageContext) {
        this.restTemplate = restTemplate;
        this.messageContext = messageContext;
    }

    public <T, R> ResponseEntity<R> post(String serviceBaseURL, String url, T request, Class<R> responseType) {
        log.info("RestClient making POST request to {}{}", serviceBaseURL, url);
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.POST, new HttpEntity<>(request, createAuthHeaders()), responseType);
    }

    public <T, R> ResponseEntity<R> put(String serviceBaseURL, String url, T request, Class<R> responseType) {
        log.info("RestClient making PUT request to {}{}", serviceBaseURL, url);
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.PUT, new HttpEntity<>(request, createAuthHeaders()), responseType);
    }

    public <T, R> ResponseEntity<R> get(String serviceBaseURL, String url, Class<R> responseType) {
        log.info("RestClient making GET request to {}{}", serviceBaseURL, url);
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), responseType);
    }

    HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(MessageContext.CORRELATION_ID_HEADER, messageContext.getCorrelationId());
        headers.add(MessageContext.GROUP_HEADER, messageContext.getGroups());
        headers.add(MessageContext.USER_ID_HEADER, messageContext.getUserId());
        return headers;
    }

}
