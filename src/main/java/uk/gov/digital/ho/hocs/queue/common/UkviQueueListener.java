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
@Profile("ukvi")
public class UkviQueueListener {

    // Contains a single queue message handler for now
    private final List<MessageHandler> queueMessageHandlers;

    private final MessageLogService messageLogService;

    private final boolean shouldIgnoreMessages;

    public UkviQueueListener(List<MessageHandler> queueMessageHandlers,
                             MessageLogService messageLogService,
                             @Value("${aws.sqs.ignore-messages:false}") boolean shouldIgnoreMessages) {
        this.queueMessageHandlers = queueMessageHandlers;
        this.messageLogService = messageLogService;
        this.shouldIgnoreMessages = shouldIgnoreMessages;
    }

    @SqsListener(value = "${aws.sqs.case-creator.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onComplaintEvent(String message,
                                 @Header("MessageId") String messageId,
                                 @Header(value = "ExternalReference", required = false) UUID externalReference) throws Exception {
        // Create message log entry
        messageLogService.createMessageLogEntry(messageId, externalReference, message);

        if (shouldIgnoreMessages) {
            log.warn("Message flagged to ignore: {}", messageId);
        } else {
            for (MessageHandler messageHandler :
                    queueMessageHandlers) {
                // Handles the only message at the minute, should be adapted to read a property from the message.
                if (messageHandler.getMessageType().equals(MessageTypes.UKVI_COMPLAINTS)) {
                    messageHandler.handleMessage(messageId, message);
                    break;
                }
            }
        }

        messageLogService.completeMessageLogEntry(messageId);
    }

}
