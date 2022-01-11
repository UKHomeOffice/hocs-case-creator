package uk.gov.digital.ho.hocs.queue.common;

import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueueListener {

    // Contains a single queue message handler for now
    private final List<MessageHandler> queueMessageHandlers;

    public QueueListener(List<MessageHandler> queueMessageHandlers) {
        this.queueMessageHandlers = queueMessageHandlers;
    }

    @SqsListener(value = "${aws.sqs.ukvi-complaint.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onComplaintEvent(String message, @Header("MessageId") String messageId) throws Exception {
        for (MessageHandler messageHandler :
                queueMessageHandlers) {
                messageHandler.handleMessage(message, messageId);
                return;
        }
    }
}
