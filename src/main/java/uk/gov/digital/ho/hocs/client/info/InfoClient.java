package uk.gov.digital.ho.hocs.client.info;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.info.dto.GetTopicsResponse;
import uk.gov.digital.ho.hocs.client.info.dto.Topic;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class InfoClient {

    private final RestClient restClient;
    private final String serviceBaseURL;

    public InfoClient(RestClient restClient, @Value("${case.creator.info-service}") String serviceBaseURL) {
        this.restClient = restClient;
        this.serviceBaseURL = serviceBaseURL;
    }

    @Cacheable(value="InfoClientGetTopicsForParent", unless = "#result.size() == 0", key="{#parentTopicUUID}")
    public List<Topic> getTopicsForParent(String messageId, UUID parentTopicUUID) {
        ResponseEntity<GetTopicsResponse> responseEntity = restClient.get(
                messageId,
                serviceBaseURL,
                String.format("/topic/all/%s", parentTopicUUID),
                GetTopicsResponse.class
        );

        return Objects.requireNonNull(responseEntity.getBody()).getTopics();
    }
}
