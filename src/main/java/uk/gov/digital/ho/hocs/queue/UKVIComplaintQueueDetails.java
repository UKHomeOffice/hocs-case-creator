package uk.gov.digital.ho.hocs.queue;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.aws.SQSQueuePrefix;

@Slf4j
@Component
@Getter
public class UKVIComplaintQueueDetails {

    private final SQSQueuePrefix sqsQueuePrefix;
    private final String ukviComplaintQueue;
    private final String dlq;
    private final String queueName;
    private final String dlQueueName;
    private final String awsSQSRegion;
    private final String awsSQSAccountId;
    private final int maximumRedeliveries;
    private final int redeliveryDelay;
    private final int backOffMultiplier;
    private final int waitTimeSeconds;
    private final int initialDelay;
    private final int pollDelay;

    @Autowired
    public UKVIComplaintQueueDetails(SQSQueuePrefix sqsQueuePrefix,
                                     @Value("${case.creator.ukvi-complaint.queue-name}") String queueName,
                                     @Value("${case.creator.ukvi-complaint.dl-queue-name}") String dlQueueName,
                                     @Value("${case.creator.sqs.region}") String awsSQSRegion,
                                     @Value("${case.creator.sqs.account-id}") String awsSQSAccountId,
                                     @Value("${case.creator.ukvi-complaint.queue-maximum-redeliveries}") int maximumRedeliveries,
                                     @Value("${case.creator.ukvi-complaint.queue-redelivery-delay}") int redeliveryDelay,
                                     @Value("${case.creator.ukvi-complaint.queue-backOff-multiplier}") int backOffMultiplier,
                                     @Value("${case.creator.ukvi-complaint.queue-wait-time-seconds}") int waitTimeSeconds,
                                     @Value("${case.creator.ukvi-complaint.queue-initial-delay}") int initialDelay,
                                     @Value("${case.creator.ukvi-complaint.queue-pollDelay}") int pollDelay) {
        this.sqsQueuePrefix = sqsQueuePrefix;
        this.queueName = queueName;
        this.dlQueueName = dlQueueName;
        this.awsSQSRegion = awsSQSRegion;
        this.awsSQSAccountId = awsSQSAccountId;
        this.maximumRedeliveries = maximumRedeliveries;
        this.redeliveryDelay = redeliveryDelay;
        this.backOffMultiplier = backOffMultiplier;
        this.waitTimeSeconds = waitTimeSeconds;
        this.initialDelay = initialDelay;
        this.pollDelay = pollDelay;

        this.ukviComplaintQueue = buildQueueName();
        this.dlq = buildDlQueueName();
    }


    private String buildQueueName() {

        String redriveTemplate = "{\"maxReceiveCount\": \"%s\", \"deadLetterTargetArn\":\"arn:aws:sqs:%s:%s:%s\"}";
        String redrivePolicy = String.format(redriveTemplate, maximumRedeliveries, awsSQSRegion, awsSQSAccountId, dlQueueName);

        String queuePropertiesTemplate = "?amazonSQSClient=#sqsClient" +
                "&messageAttributeNames=All" +
                "&redrivePolicy=%s" +
                "&waitTimeSeconds=%s" +
                "&backoffIdleThreshold=1" +
                "&backoffMultiplier=%s" +
                "&initialDelay=%s" +
                "&delay=%s";
        String queueProperties = String.format(queuePropertiesTemplate, redrivePolicy, waitTimeSeconds, backOffMultiplier, initialDelay, pollDelay);

        return sqsQueuePrefix.getPrefix() + queueName + queueProperties;
    }

    private String buildDlQueueName() {

        String dlQueueProperties = "?amazonSQSClient=#sqsClient&messageAttributeNames=All";

        return sqsQueuePrefix.getPrefix() + dlQueueName + dlQueueProperties;
    }
}