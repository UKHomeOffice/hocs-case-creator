package uk.gov.digital.ho.hocs.client.info;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.info.dto.GetTopicsResponse;
import uk.gov.digital.ho.hocs.client.info.dto.Topic;

import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InfoClientTest {

    private final String serviceUrl = "http://localhost:8085";
    private InfoClient infoClient;
    @Mock
    private RestClient restClient;
    private String messageId;

    @Before
    public void setUp() {
        infoClient = new InfoClient(restClient, serviceUrl);
        messageId = UUID.randomUUID().toString();
    }

    @Test
    public void shouldGetStageForCase() {
        UUID parentTopicUUID = UUID.randomUUID();

        GetTopicsResponse response = new GetTopicsResponse(List.of(
                new Topic("Topic one", UUID.randomUUID(), false),
                new Topic("Topic two", UUID.randomUUID(), false)
        ));

        ResponseEntity<GetTopicsResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(restClient.get(
                messageId,
                serviceUrl,
                String.format("/topic/all/%s", parentTopicUUID),
                GetTopicsResponse.class
        )).thenReturn(responseEntity);

        List<Topic> topics = infoClient.getTopicsForParent(messageId, parentTopicUUID);

        assertEquals(topics, response.getTopics());
    }
}
