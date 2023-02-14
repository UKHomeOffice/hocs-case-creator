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
@Profile("migration")
public class MigrationQueueListener {

    private final List<MessageHandler> queueMessageHandlers;

    private final boolean shouldIgnoreMessages;

    public MigrationQueueListener(List<MessageHandler> queueMessageHandlers,
                                  @Value("${aws.sqs.ignore-messages:false}") boolean shouldIgnoreMessages) {
        this.queueMessageHandlers = queueMessageHandlers;
        this.shouldIgnoreMessages = shouldIgnoreMessages;
    }

    @SqsListener(value = "${aws.sqs.case-migrator.url}", deletionPolicy = SqsMessageDeletionPolicy.NO_REDRIVE)
    public void onMigrationEvent(String message, @Header("MessageId") String messageId) throws Exception {
        if (shouldIgnoreMessages) {
            log.warn("Message flagged to ignore: {}", messageId);
            return;
        }

        for (MessageHandler messageHandler :
                queueMessageHandlers) {
            if (messageHandler.getMessageType().equals(MessageTypes.MIGRATION)) {
                messageHandler.handleMessage(message, messageId);
                break;
            }
        }
    }
}
