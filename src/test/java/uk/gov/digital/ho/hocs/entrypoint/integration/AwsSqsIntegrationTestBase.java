package uk.gov.digital.ho.hocs.entrypoint.integration;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("local")
public class AwsSqsIntegrationTestBase {

    private static final String APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE = "ApproximateNumberOfMessagesNotVisible";

    @Autowired
    public AmazonSQSAsync amazonSQSAsync;

    @Value("${aws.sqs.queue.url}")
    protected String queueUrl;
    
    @Before
    public void setup() {
        amazonSQSAsync.purgeQueue(new PurgeQueueRequest(queueUrl));
    }

    @After
    public void teardown() {
        amazonSQSAsync.purgeQueue(new PurgeQueueRequest(queueUrl));
    }

    public int getNumberOfMessagesNotVisibleOnQueue() {
        return getValueFromQueue(queueUrl, APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE);
    }

    private int getValueFromQueue(String queue, String attribute) {
        var queueAttributes = amazonSQSAsync.getQueueAttributes(queue, List.of(attribute));
        var messageCount = queueAttributes.getAttributes().get(attribute);
        return messageCount == null ? 0 : Integer.parseInt(messageCount);
    }

}
