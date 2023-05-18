package uk.gov.digital.ho.hocs.application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RestClientTest {

    private final String baseUrl = "http://service";
    private final String url = "/url";
    private final String request = "body";
    private final String user = "U1";
    private final String group = "G1";
    @Mock
    private RestTemplate restTemplate;
    private RestClient restClient;
    private String expectedUrl;
    private String messageId;
    private HttpHeaders expectedAuthHeaders;

    @Before
    public void setUp() {
        messageId = UUID.randomUUID().toString();
        restClient = new RestClient(restTemplate, user, group);
        expectedUrl = String.format("%s%s", baseUrl, url);
        expectedAuthHeaders = restClient.createAuthHeaders(messageId);
    }

    @Test
    public void shouldPost() {
        restClient.post(messageId, baseUrl, url, request, String.class);

        HttpEntity<String> expectedRequestEntity = new HttpEntity<>(request, expectedAuthHeaders);

        verify(restTemplate).exchange(expectedUrl, HttpMethod.POST, expectedRequestEntity, String.class);
    }

    @Test
    public void shouldPut() {
        restClient.put(messageId, baseUrl, url, request, String.class);

        HttpEntity<String> expectedRequestEntity = new HttpEntity<>(request, expectedAuthHeaders);

        verify(restTemplate).exchange(expectedUrl, HttpMethod.PUT, expectedRequestEntity, String.class);
    }

    @Test
    public void shouldGet() {
        restClient.get(messageId, baseUrl, url, String.class);

        HttpEntity<String> expectedRequestEntity = new HttpEntity<>(null, expectedAuthHeaders);

        verify(restTemplate).exchange(expectedUrl, HttpMethod.GET, expectedRequestEntity, String.class);
    }
}
