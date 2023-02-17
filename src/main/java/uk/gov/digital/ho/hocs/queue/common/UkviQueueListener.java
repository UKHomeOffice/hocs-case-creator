package uk.gov.digital.ho.hocs.queue.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.LogEvent;
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.model.Status;
import uk.gov.digital.ho.hocs.queue.complaints.ukvi.UKVIComplaintMessageHandler;
import uk.gov.digital.ho.hocs.service.MessageLogService;

import java.util.UUID;

@Service
@Slf4j
@Profile("ukvi")
public class UkviQueueListener implements QueueListener {

    private final MessageHandler messageHandler;

    private final MessageLogService messageLogService;

    private final boolean shouldIgnoreMessages;

    public UkviQueueListener(UKVIComplaintMessageHandler messageHandler,
                             MessageLogService messageLogService,
                             @Value("${aws.sqs.ignore-messages:false}") boolean shouldIgnoreMessages) {
        this.messageHandler = messageHandler;
        this.messageLogService = messageLogService;
        this.shouldIgnoreMessages = shouldIgnoreMessages;
    }

    @SqsListener(value = "${aws.sqs.case-creator.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onMessageReceived(String message,
                                 @Header("MessageId") String messageId,
                                 @Header(value = "MessageType", required = false) MessageType messageType,
                                 @Header(value = "ExternalReference", required = false) UUID externalReference) throws Exception {
        // Create message log entry
        messageLogService.createMessageLogEntry(messageId, externalReference, message);

        if (shouldIgnoreMessages) {
            log.warn("Message flagged to ignore: {}", messageId);
            messageLogService.updateMessageLogEntryStatus(messageId, Status.IGNORED);
            return;
        }

        if (messageType == null ||
                messageHandler.getMessageType().equals(messageType)) {
            messageHandler.handleMessage(messageId, message);
        } else {
            messageLogService.updateMessageLogEntryStatus(messageId, Status.MESSAGE_TYPE_INVALID);
            throw new ApplicationExceptions.InvalidMessageTypeException(String.format("Invalid message type %s", messageType), LogEvent.INVALID_MESSAGE_TYPE);
        }

        messageLogService.completeMessageLogEntry(messageId);
    }

}
