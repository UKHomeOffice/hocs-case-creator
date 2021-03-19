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
                                     @Value("${case.creator.ukvi-complaint.queue-pollDelay}") int pollDelay) {
        this.sqsQueuePrefix = sqsQueuePrefix;
        this.queueName = queueName;
        this.dlQueueName = dlQueueName;
        this.awsSQSRegion = awsSQSRegion;
        this.awsSQSAccountId = awsSQSAccountId;
        this.maximumRedeliveries = maximumRedeliveries;
        this.redeliveryDelay = redeliveryDelay;
        this.backOffMultiplier = backOffMultiplier;
        this.pollDelay = pollDelay;

        this.ukviComplaintQueue = buildQueueName();
        this.dlq = buildDlQueueName();
    }


    private String buildQueueName() {

        //case.creator.ukvi-complaint.redrive-policy={"maxReceiveCount": "${case.creator.ukvi-complaint.queue-maximum-redeliveries}", "deadLetterTargetArn":"arn:aws:sqs:${case.creator.sqs.region}:${case.creator.sqs.account-id}:${case.creator.ukvi-complaint.dl-queue-name}"}
        String redriveTemplate = "{\"maxReceiveCount\": \"%s\", \"deadLetterTargetArn\":\"arn:aws:sqs:%s:%s:%s\"}";
        String redrivePolicy = String.format(redriveTemplate, maximumRedeliveries, awsSQSRegion, awsSQSAccountId, dlQueueName);

        //case.creator.ukvi-complaint.queue-properties=amazonSQSClient=#sqsClient&messageAttributeNames=All&redrivePolicy=${case.creator.ukvi-complaint.redrive-policy}&waitTimeSeconds=20&backoffIdleThreshold=1&backoffMultiplier=${case.creator.ukvi-complaint.queue-backOff-multiplier}&initialDelay=5000&delay=${case.creator.ukvi-complaint.queue-pollDelay}
        String queuePropertiesTemplate = "?amazonSQSClient=#sqsClient&messageAttributeNames=All&redrivePolicy=%s&waitTimeSeconds=20&backoffIdleThreshold=1&backoffMultiplier=%s&initialDelay=5000&delay=%s";
        String queueProperties = String.format(queuePropertiesTemplate, redrivePolicy, backOffMultiplier, pollDelay);

        //case.creator.ukvi-complaint.queue=aws-sqs://${case.creator.ukvi-complaint.queue-name}?${case.creator.ukvi-complaint.queue-properties}
        return sqsQueuePrefix.getPrefix() + queueName + queueProperties;
    }

    private String buildDlQueueName() {

        //case.creator.ukvi-complaint.dl-queue-properties=amazonSQSClient=#sqsClient&messageAttributeNames=All
        String dlQueueProperties = "?amazonSQSClient=#sqsClient&messageAttributeNames=All";

        //case.creator.ukvi-complaint.dl-queue=aws-sqs://${case.creator.ukvi-complaint.dl-queue-name}?${case.creator.ukvi-complaint.dl-queue-properties}
        return sqsQueuePrefix.getPrefix() + dlQueueName + dlQueueProperties;
    }
}
