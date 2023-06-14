package uk.gov.digital.ho.hocs.domain.queue.migration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.client.info.InfoClient;
import uk.gov.digital.ho.hocs.client.info.dto.Topic;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TopicMapper {

    private final InfoClient infoClient;
    private final UUID parentTopicId;

    private Map<String, UUID> topicMapping = null;

    public TopicMapper(
        InfoClient infoClient,
        @Value("${case.creator.migration.parent-topic-uuid}") UUID parentTopicId
    ) {
        this.infoClient = infoClient;
        this.parentTopicId = parentTopicId;
    }

    public Optional<UUID> getTopicId(String messageId, String topicText) {
        return Optional.ofNullable(
            this.getTopicMapping(messageId)
                .getOrDefault(topicText, null)
        );
    }

    private Map<String, UUID> getTopicMapping(String messageId) {
        if (topicMapping == null) {
            topicMapping =
                infoClient.getTopicsForParent(messageId, parentTopicId).stream()
                          .collect(Collectors.toMap(Topic::getDisplayName, Topic::getUuid));
        }
        return topicMapping;
    }
}
