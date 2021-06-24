package uk.gov.digital.ho.hocs.queue.ukvi;

import org.junit.Test;
import uk.gov.digital.ho.hocs.aws.LocalStackConfiguration;
import uk.gov.digital.ho.hocs.aws.SQSConfiguration;

import static junit.framework.TestCase.assertEquals;

public class UKVIComplaintQueueBuilderTest {

    String queueName = "complaint-queue";
    String dlQueueName = "complaint-dlq";
    String region = "eu-west-2";
    String accountId = "12345";
    int maximumRedeliveries = 1;
    int redeliveryDelay = 2;
    int backoffIdleThreshold = 3;
    int backOffMultiplier = 4;
    int waitTimeSeconds = 5;
    int maxMessagesPerPoll = 1;
    int initialDelay = 6;
    int pollDelay = 7;

    @Test
    public void shouldReturnCorrectQueueForLocalConfig() {

        LocalStackConfiguration localStackConfiguration = new LocalStackConfiguration();

        UKVIComplaintQueueBuilder queueBuilder = new UKVIComplaintQueueBuilder(
                localStackConfiguration.queuePrefix(),
                queueName,
                dlQueueName,
                region,
                accountId,
                maximumRedeliveries,
                redeliveryDelay,
                backoffIdleThreshold,
                backOffMultiplier,
                waitTimeSeconds,
                maxMessagesPerPoll, 
                initialDelay,
                pollDelay);

        assertEquals(String.format("aws-sqs://%s?amazonSQSClient=#sqsClient&messageAttributeNames=All" +
                "&redrivePolicy={\"maxReceiveCount\": \"%d\", \"deadLetterTargetArn\":\"arn:aws:sqs:%s:%s:%s\"}" +
                "&waitTimeSeconds=%d" +
                "&backoffIdleThreshold=%d" +
                "&backoffMultiplier=%d" +
                "&maxMessagesPerPoll=%d" +
                "&initialDelay=%d" +
                "&delay=%d",
                queueName, maximumRedeliveries, region, accountId, dlQueueName, waitTimeSeconds,
                backoffIdleThreshold, backOffMultiplier, maxMessagesPerPoll, initialDelay, pollDelay), queueBuilder.getQueue());
    }

    @Test
    public void shouldReturnCorrectQueueForSQSConfig() {

        SQSConfiguration sqsConfiguration = new SQSConfiguration();

        UKVIComplaintQueueBuilder queueBuilder = new UKVIComplaintQueueBuilder(
                sqsConfiguration.queuePrefix(region, accountId),
                queueName,
                dlQueueName,
                region,
                accountId,
                maximumRedeliveries,
                redeliveryDelay,
                backoffIdleThreshold,
                backOffMultiplier,
                waitTimeSeconds,
                maxMessagesPerPoll, 
                initialDelay,
                pollDelay);

        assertEquals(String.format("aws-sqs://arn:aws:sqs:%s:%s:%s?amazonSQSClient=#sqsClient&messageAttributeNames=All" +
                "&redrivePolicy={\"maxReceiveCount\": \"%d\", \"deadLetterTargetArn\":\"arn:aws:sqs:%s:%s:%s\"}" +
                "&waitTimeSeconds=%d" +
                "&backoffIdleThreshold=%d" +
                "&backoffMultiplier=%d" +
                "&maxMessagesPerPoll=%d" +
                "&initialDelay=%d" +
                "&delay=%d",
                region, accountId, queueName, maximumRedeliveries, region, accountId, dlQueueName, waitTimeSeconds,
                backoffIdleThreshold, backOffMultiplier, maxMessagesPerPoll, initialDelay, pollDelay
                ), queueBuilder.getQueue());
    }

}
