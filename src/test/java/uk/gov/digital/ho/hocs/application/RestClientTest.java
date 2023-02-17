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

    @Mock
    private RestTemplate restTemplate;

    private RestClient restClient;
    private String baseUrl;
    private String url;
    private String request;
    private String expectedUrl;
    private HttpHeaders expectedAuthHeaders;

    @Before
    public void setUp() {
        RequestData requestData = new RequestData();
        MessageContext messageContext = new MessageContext(requestData,  "u1", "g1", "t1");
        restClient = new RestClient(restTemplate, messageContext);
        baseUrl = "http://service";
        url = "/url";
        expectedUrl = String.format("%s%s", baseUrl, url);
        request = "body";
        messageContext.initialiseContext(UUID.randomUUID().toString());
        expectedAuthHeaders = restClient.createAuthHeaders();
    }

    @Test
    public void shouldPost() {

        restClient.post(baseUrl, url, request, String.class);

        HttpEntity<String> expectedRequestEntity = new HttpEntity<>(request, expectedAuthHeaders);

        verify(restTemplate).exchange(expectedUrl, HttpMethod.POST, expectedRequestEntity, String.class);
    }

    @Test
    public void shouldPut() {

        restClient.put(baseUrl, url, request, String.class);

        HttpEntity<String> expectedRequestEntity = new HttpEntity<>(request, expectedAuthHeaders);

        verify(restTemplate).exchange(expectedUrl, HttpMethod.PUT, expectedRequestEntity, String.class);
    }

    @Test
    public void shouldGet() {

        restClient.get(baseUrl, url, String.class);

        HttpEntity<String> expectedRequestEntity = new HttpEntity<>(null, expectedAuthHeaders);

        verify(restTemplate).exchange(expectedUrl, HttpMethod.GET, expectedRequestEntity, String.class);
    }
}
