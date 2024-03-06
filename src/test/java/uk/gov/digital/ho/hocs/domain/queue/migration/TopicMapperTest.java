package uk.gov.digital.ho.hocs.domain.queue.migration;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.client.info.InfoClient;
import uk.gov.digital.ho.hocs.client.info.dto.Topic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("migration")
public class TopicMapperTest {

    @Mock
    private InfoClient infoClient;

    private UUID parentTopicUUID;

    private String messageId;

    private List<Topic> topics;

    private TopicMapper topicMapper;

    @Before
    public void setUp() {
        parentTopicUUID = UUID.randomUUID();
        messageId = UUID.randomUUID().toString();
        topicMapper = new TopicMapper(infoClient, parentTopicUUID);

        topics = List.of(
                new Topic("Topic one", UUID.randomUUID(), false),
                new Topic("Topic two", UUID.randomUUID(), false)
        );
        when(infoClient.getTopicsForParent(messageId, parentTopicUUID)).thenReturn(topics);
    }

    @Test
    public void getTopicId_returnsTheMatchingUUID() {
        Optional<UUID> maybeUUID = topicMapper.getTopicId(messageId, "Topic one");
        assertEquals(Optional.of(topics.get(0).getUuid()), maybeUUID);
    }

    @Test
    public void getTopicId_returnsEmptyOptionOnNoMatch() {
        Optional<UUID> maybeUUID = topicMapper.getTopicId(messageId, "Topic missing");
        assertEquals(Optional.empty(), maybeUUID);
    }

    @Test
    public void getTopicId_returnsEmptyOptionNullText() {
        Optional<UUID> maybeUUID = topicMapper.getTopicId(messageId, null);
        assertEquals(Optional.empty(), maybeUUID);
    }

    @Test
    public void responseFromInfoServiceIsCached() {
        topicMapper.getTopicId(messageId, "Topic one");
        Optional<UUID> maybeUUID = topicMapper.getTopicId(UUID.randomUUID().toString(), "Topic two");

        verify(infoClient, times(1)).getTopicsForParent(messageId, parentTopicUUID);
        verifyNoMoreInteractions(infoClient);

        assertEquals(Optional.of(topics.get(1).getUuid()), maybeUUID);
    }
}
