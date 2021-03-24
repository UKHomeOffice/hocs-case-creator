package uk.gov.digital.ho.hocs.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class RestClient {

    private final RestTemplate restTemplate;
    private final String basicAuthString;
    private final ClientContext clientContext;

    @Autowired
    public RestClient(RestTemplate restTemplate,
                      @Value("${case.creator.basicauth}") String basicAuthString,
                      ClientContext clientContext) {
        this.restTemplate = restTemplate;
        this.basicAuthString = basicAuthString;
        this.clientContext = clientContext;
    }

    public <T, R> ResponseEntity<R> post(String serviceBaseURL, String url, T request, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.POST, new HttpEntity<>(request, createAuthHeaders(clientContext)), responseType);
    }

    public <T, R> ResponseEntity<R> put(String serviceBaseURL, String url, T request, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.PUT, new HttpEntity<>(request, createAuthHeaders(clientContext)), responseType);
    }

    public <T, R> ResponseEntity<R> get(String serviceBaseURL, String url, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders(clientContext)), responseType);
    }

    HttpHeaders createAuthHeaders(ClientContext clientContext) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION, getBasicAuthBase64(basicAuthString));
        headers.add(ClientContext.CORRELATION_ID_HEADER, clientContext.getCorrelationId());
        headers.add(ClientContext.GROUP_HEADER, clientContext.getGroups());
        headers.add(ClientContext.USER_ID_HEADER, clientContext.getUserId());
        return headers;
    }

    String getBasicAuthBase64(String basicAuth) {
        return String.format("Basic %s", Base64.getEncoder().encodeToString(basicAuth.getBytes(StandardCharsets.UTF_8)));
    }

}
