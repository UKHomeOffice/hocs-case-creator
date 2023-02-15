package uk.gov.digital.ho.hocs.queue.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@Profile("ukvi")
public class UkviQueueListener {

    // Contains a single queue message handler for now
    private final List<MessageHandler> queueMessageHandlers;

    private final boolean shouldIgnoreMessages;

    public UkviQueueListener(List<MessageHandler> queueMessageHandlers,
                            @Value("${aws.sqs.ignore-messages:false}") boolean shouldIgnoreMessages) {
        this.queueMessageHandlers = queueMessageHandlers;
        this.shouldIgnoreMessages = shouldIgnoreMessages;
    }

    @SqsListener(value = "${aws.sqs.case-creator.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onComplaintEvent(String message, @Header("MessageId") String messageId) throws Exception {
        if (shouldIgnoreMessages) {
            log.warn("Message flagged to ignore: {}", messageId);
            return;
        }

        for (MessageHandler messageHandler :
                queueMessageHandlers) {
            // Handles the only message at the minute, should be adapted to read a property from the message.
            if (messageHandler.getMessageType().equals(MessageTypes.UKVI_COMPLAINTS)) {
                messageHandler.handleMessage(message, messageId);
                break;
            }
        }
    }

}
