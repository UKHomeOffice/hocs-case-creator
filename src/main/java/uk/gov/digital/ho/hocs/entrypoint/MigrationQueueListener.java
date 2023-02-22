package uk.gov.digital.ho.hocs.entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.LogEvent;
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageHandler;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;
import uk.gov.digital.ho.hocs.domain.queue.migration.MigrationMessageHandler;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

import java.util.UUID;

@Service
@Slf4j
@Profile("migration")
public class MigrationQueueListener implements QueueListener {

    private final MessageHandler messageHandler;

    private final MessageLogService messageLogService;

    private final boolean shouldIgnoreMessages;

    public MigrationQueueListener(MigrationMessageHandler messageHandler,
                                  MessageLogService messageLogService,
                                  @Value("${aws.sqs.ignore-messages:false}") boolean shouldIgnoreMessages) {
        this.messageHandler = messageHandler;
        this.messageLogService = messageLogService;
        this.shouldIgnoreMessages = shouldIgnoreMessages;
    }

    @SqsListener(value = "${aws.sqs.case-migrator.url}", deletionPolicy = SqsMessageDeletionPolicy.NO_REDRIVE)
    public void onMessageReceived(String message,
                                  @Header("MessageId") String messageId,
                                  @Header(value = "MessageType", required = false) MessageType messageType,
                                  @Header(value = "ExternalReference", required = false) UUID externalReference) throws Exception {
        if (shouldIgnoreMessages) {
            log.warn("Message flagged to ignore: {}", messageId);
            messageLogService.createMessageLogEntry(messageId, externalReference, message, Status.IGNORED);
            return;
        }

        // Create message log entry
        messageLogService.createMessageLogEntry(messageId, externalReference, message);

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