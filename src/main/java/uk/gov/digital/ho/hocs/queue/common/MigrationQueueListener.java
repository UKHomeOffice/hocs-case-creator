package uk.gov.digital.ho.hocs.queue.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.service.MessageLogService;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Profile("migration")
public class MigrationQueueListener {

    private final List<MessageHandler> queueMessageHandlers;

    private final MessageLogService messageLogService;

    private final boolean shouldIgnoreMessages;

    public MigrationQueueListener(List<MessageHandler> queueMessageHandlers,
                                  MessageLogService messageLogService,
                                  @Value("${aws.sqs.ignore-messages:false}") boolean shouldIgnoreMessages) {
        this.queueMessageHandlers = queueMessageHandlers;
        this.messageLogService = messageLogService;
        this.shouldIgnoreMessages = shouldIgnoreMessages;
    }

    @SqsListener(value = "${aws.sqs.case-migrator.url}", deletionPolicy = SqsMessageDeletionPolicy.NO_REDRIVE)
    public void onMigrationEvent(String message,
                                 @Header("MessageId") String messageId,
                                 @Header(value = "ExternalReference", required = false) UUID externalReference) throws Exception {
        // Create message log entry
        messageLogService.createMessageLogEntry(messageId, externalReference, message);

        if (shouldIgnoreMessages) {
            log.warn("Message flagged to ignore: {}", messageId);
        } else {
            for (MessageHandler messageHandler :
                    queueMessageHandlers) {
                if (messageHandler.getMessageType().equals(MessageTypes.MIGRATION)) {
                    messageHandler.handleMessage(messageId, message);
                    break;
                }
            }
        }

        messageLogService.completeMessageLogEntry(messageId);
    }
}
