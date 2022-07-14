package uk.gov.digital.ho.hocs.queue.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty( name = "case-creator.mode", havingValue = "migration", matchIfMissing = false)
public class MigrationQueueListener {

    private final List<BaseMessageHandler> queueMessageHandlers;

    public MigrationQueueListener(List<BaseMessageHandler> queueMessageHandlers) {
        this.queueMessageHandlers = queueMessageHandlers;
    }

    @SqsListener(value = "${aws.sqs.case-creator.url}", deletionPolicy = SqsMessageDeletionPolicy.NO_REDRIVE)
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
