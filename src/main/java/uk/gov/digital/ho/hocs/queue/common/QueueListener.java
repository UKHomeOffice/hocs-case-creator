package uk.gov.digital.ho.hocs.queue.common;

import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.queue.ukvi.UKVIComplaintMessageHandler;

@Service
public class QueueListener {

    private final UKVIComplaintMessageHandler ukviComplaintMessageHandler;

    public QueueListener(UKVIComplaintMessageHandler ukviComplaintMessageHandler) {
        this.ukviComplaintMessageHandler = ukviComplaintMessageHandler;
    }

    @SqsListener(value = "${aws.sqs.ukvi-complaint.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onComplaintEvent(String message, @Header("MessageId") String messageId) throws Exception {
        ukviComplaintMessageHandler.handleMessage(message, messageId);
    }
}
