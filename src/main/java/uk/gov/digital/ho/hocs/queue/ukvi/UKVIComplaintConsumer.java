package uk.gov.digital.ho.hocs.queue.ukvi;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.sqs.SqsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Slf4j
@Component
public class UKVIComplaintConsumer extends RouteBuilder {

    private final UKVIComplaintService ukviComplaintService;
    private final UKVIComplaintQueueBuilder queueDetails;
    private final UKVIComplaintValidator ukviComplaintValidator;
    private final Integer shutdownWaitSeconds;

    @Autowired
    public UKVIComplaintConsumer(UKVIComplaintService ukviComplaintService,
                                 UKVIComplaintQueueBuilder queueDetails,
                                 UKVIComplaintValidator ukviComplaintValidator,
                                 @Value("${case.creator.shutdown-delay-seconds}") Integer shutdownWaitSeconds) {
        this.ukviComplaintService = ukviComplaintService;
        this.queueDetails = queueDetails;
        this.ukviComplaintValidator = ukviComplaintValidator;
        this.shutdownWaitSeconds = shutdownWaitSeconds;
    }

    @Override
    public void configure() {

        errorHandler(deadLetterChannel(queueDetails.getDlq())
                .log(log)
                .loggingLevel(LoggingLevel.DEBUG)
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .useOriginalMessage()
                .maximumRedeliveries(queueDetails.getMaximumRedeliveries())
                .redeliveryDelay(queueDetails.getRedeliveryDelay())
                .backOffMultiplier(queueDetails.getBackOffMultiplier())
                .asyncDelayedRedelivery()
                .logRetryStackTrace(false)
                .onPrepareFailure(exchange -> {
                    exchange.getIn().setHeader("FailureMessage", exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
                            Exception.class).getMessage());
                    exchange.getIn().setHeader(SqsConstants.RECEIPT_HANDLE, exchangeProperty(SqsConstants.RECEIPT_HANDLE));
                }));

        from(queueDetails.getQueue())
                .setProperty(SqsConstants.RECEIPT_HANDLE, header(SqsConstants.RECEIPT_HANDLE))
                .log(LoggingLevel.INFO, log, "UKVI Complaint received, MessageId : ${headers.CamelAwsSqsMessageId}")
                .bean(ukviComplaintValidator, "validate(${body}, ${headers.CamelAwsSqsMessageId})")
                .bean(ukviComplaintService, "createComplaint(${body}, ${headers.CamelAwsSqsMessageId})")
                .log(LoggingLevel.INFO, log, "UKVI Complaint processed, MessageId : ${headers.CamelAwsSqsMessageId}")
                .setHeader(SqsConstants.RECEIPT_HANDLE, exchangeProperty(SqsConstants.RECEIPT_HANDLE));
    }

    @PreDestroy
    public void onExit() {
        log.info("hocs-case-creator stopping gracefully");
    }

}
