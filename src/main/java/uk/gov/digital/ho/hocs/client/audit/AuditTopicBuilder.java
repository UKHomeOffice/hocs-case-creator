package uk.gov.digital.ho.hocs.client.audit;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.aws.SNSTopicPrefix;

@Slf4j
@Component
@Getter
public class AuditTopicBuilder {

    private static final String SNS_TOPIC_SUFFIX = "?amazonSNSClient=#auditSnsClient";
    private final SNSTopicPrefix topicPrefix;
    private final String topicName;
    private final String topic;

    @Autowired
    public AuditTopicBuilder(SNSTopicPrefix topicPrefix,
                             @Value("${audit.sns.topic-name}") String topicName) {
        this.topicPrefix = topicPrefix;
        this.topicName = topicName;
        topic = buildTopic();
    }

    private String buildTopic() {
        return topicPrefix.getPrefix() + topicName + SNS_TOPIC_SUFFIX;
    }
}
