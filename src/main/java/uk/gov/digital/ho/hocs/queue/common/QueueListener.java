package uk.gov.digital.ho.hocs.queue.common;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class QueueListener {

    // Contains a single queue message handler for now
    private final List<BaseMessageHandler> queueMessageHandlers;

    public QueueListener(List<BaseMessageHandler> queueMessageHandlers) {
        this.queueMessageHandlers = queueMessageHandlers;
    }

    @SqsListener(value = "${aws.sqs.case-creator.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onComplaintEvent(String message, @Header("MessageId") String messageId) throws Exception {
        for (BaseMessageHandler messageHandler :
                queueMessageHandlers) {
            // Handles the only message at the minute, should be adapted to read a property from the message.
            if (messageHandler.getMessageType().equals(MessageTypes.UKVI_COMPLAINTS)) {
                if (!messageHandler.shouldIgnoreMessage()) {
                    messageHandler.handleMessage(message, messageId);
                } else {
                    log.warn("Message flagged to ignore: {}", messageId);
                }
            }
        }
    }

    @SqsListener(deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onMigrationEvent(String message, @Header("MessageId") String messageId) throws Exception {
        for (BaseMessageHandler messageHandler :
                queueMessageHandlers) {

            if (messageHandler.getMessageType().equals(MessageTypes.MIGRATION)) {
                if (!messageHandler.shouldIgnoreMessage()) {
                    messageHandler.handleMessage(message, messageId);
                } else {
                    log.warn("Message flagged to ignore: {}", messageId);
                }
            }
        }
    }


}
