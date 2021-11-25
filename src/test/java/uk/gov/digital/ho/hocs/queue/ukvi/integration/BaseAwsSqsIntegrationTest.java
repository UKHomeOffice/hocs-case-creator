package uk.gov.digital.ho.hocs.queue.ukvi.integration;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.application.properties.AwsSqsProperties;

import java.util.List;

@ActiveProfiles("local")
public class BaseAwsSqsIntegrationTest {

    private static final String APPROXIMATE_NUMBER_OF_MESSAGES = "ApproximateNumberOfMessages";
    private static final String APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE = "ApproximateNumberOfMessagesNotVisible";

    @Autowired
    public AmazonSQSAsync amazonSQSAsync;

    @Autowired
    public AwsSqsProperties awsSqsProperties;

    @BeforeEach
    public void setup() {
        amazonSQSAsync.purgeQueue(new PurgeQueueRequest(awsSqsProperties.getUkviComplaint().getUrl()));
    }

    @AfterEach
    public void teardown() {
        amazonSQSAsync.purgeQueue(new PurgeQueueRequest(awsSqsProperties.getUkviComplaint().getUrl()));
    }

    public int getNumberOfMessagesOnQueue() {
        return getValueFromQueue(awsSqsProperties.getUkviComplaint().getUrl(), APPROXIMATE_NUMBER_OF_MESSAGES);
    }

    public int getNumberOfMessagesNotVisibleOnQueue() {
        return getValueFromQueue(awsSqsProperties.getUkviComplaint().getUrl(), APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE);
    }

    private int getValueFromQueue(String queue, String attribute) {
        var queueAttributes = amazonSQSAsync.getQueueAttributes(queue, List.of(attribute));
        var messageCount = queueAttributes.getAttributes().get(attribute);
        return messageCount == null ? 0 : Integer.parseInt(messageCount);
    }


}
