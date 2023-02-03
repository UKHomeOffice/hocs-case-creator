package uk.gov.digital.ho.hocs.queue.common;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.repositories.MessageLogRepository;
import uk.gov.digital.ho.hocs.service.MessageLogService;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "case-creator.mode", havingValue = "creation", matchIfMissing = true)
public class QueueListener {

    // Contains a single queue message handler for now
    private final List<BaseMessageHandler> queueMessageHandlers;

    private final MessageLogService messageLogService;

    public QueueListener(List<BaseMessageHandler> queueMessageHandlers, MessageLogService messageLogService) {
        this.queueMessageHandlers = queueMessageHandlers;
        this.messageLogService = messageLogService;
    }

    @SqsListener(value = "${aws.sqs.case-creator.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onComplaintEvent(String message,
                                 @Header("MessageId") String messageId,
                                 @Header(value = "ExternalReference", required = false) UUID externalReference) throws Exception {
        // Create message log entry
        messageLogService.createMessageLogEntry(messageId, externalReference, message);

        for (BaseMessageHandler messageHandler :
                queueMessageHandlers) {
            // Handles the only message at the minute, should be adapted to read a property from the message.
            if (messageHandler.getMessageType().equals(MessageTypes.UKVI_COMPLAINTS)) {
                if (!messageHandler.shouldIgnoreMessage()) {
                    messageHandler.handleMessage(message, messageId);
                    break;
                } else {
                    log.warn("Message flagged to ignore: {}", messageId);
                }
            }
        }

        messageLogService.completeMessageLogEntry(messageId);
    }

}
