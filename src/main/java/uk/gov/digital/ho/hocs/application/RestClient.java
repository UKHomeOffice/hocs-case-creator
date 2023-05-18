package uk.gov.digital.ho.hocs.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private static final String USER_ID_HEADER = "X-Auth-UserId";

    private static final String GROUP_HEADER = "X-Auth-Groups";

    private final String userId;

    private final String group;

    public RestClient(RestTemplate restTemplate,
                      @Value("${case.creator.identity.user}") String user,
                      @Value("${case.creator.identity.group}") String group) {
        this.restTemplate = restTemplate;

        this.userId = user;
        this.group = group;
    }

    public <T, R> ResponseEntity<R> post(String messageId, String serviceBaseURL, String url, T request, Class<R> responseType) {
        log.info("RestClient making POST request to {}{}", serviceBaseURL, url);
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.POST, new HttpEntity<>(request, createAuthHeaders(messageId)), responseType);
    }

    public <T, R> ResponseEntity<R> put(String messageId, String serviceBaseURL, String url, T request, Class<R> responseType) {
        log.info("RestClient making PUT request to {}{}", serviceBaseURL, url);
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.PUT, new HttpEntity<>(request, createAuthHeaders(messageId)), responseType);
    }

    public <T, R> ResponseEntity<R> get(String messageId, String serviceBaseURL, String url, Class<R> responseType) {
        log.info("RestClient making GET request to {}{}", serviceBaseURL, url);
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders(messageId)), responseType);
    }

    HttpHeaders createAuthHeaders(String messageId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add(RequestData.CORRELATION_ID_HEADER, messageId);
        headers.add(USER_ID_HEADER, userId);
        headers.add(GROUP_HEADER, group);

        return headers;
    }

}
