package uk.gov.digital.ho.hocs.entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

import java.util.UUID;

@Service
@Slf4j
@Profile("ukvi")
public class UkviQueueListener implements QueueListener {

    private final MessageLogService messageLogService;

    private final boolean shouldIgnoreMessages;

    public UkviQueueListener(MessageLogService messageLogService,
                             @Value("${aws.sqs.ignore-messages:false}") boolean shouldIgnoreMessages) {
        this.messageLogService = messageLogService;
        this.shouldIgnoreMessages = shouldIgnoreMessages;
    }

    @SqsListener(value = "${aws.sqs.case-creator.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onMessageReceived(String message,
                                  @Header("MessageId") String messageId,
                                  @Header(value = "MessageType", required = false) MessageType messageType,
                                  @Header(value = "ExternalReference", required = false) UUID externalReference) {
        if (shouldIgnoreMessages) {
            messageLogService.createMessageLogEntry(messageId, externalReference, messageType, message, Status.IGNORED);
            return;
        }

        // Create message log entry
        messageLogService.createMessageLogEntry(messageId, externalReference, messageType, message);
    }

}
