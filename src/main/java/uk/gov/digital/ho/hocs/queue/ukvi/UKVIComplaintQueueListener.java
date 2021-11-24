package uk.gov.digital.ho.hocs.queue.ukvi;

import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class UKVIComplaintQueueListener {

    private final UKVIComplaintService ukviComplaintService;
    private final UKVIComplaintValidator ukviComplaintValidator;

    public UKVIComplaintQueueListener(UKVIComplaintService ukviComplaintService,
                                      UKVIComplaintValidator ukviComplaintValidator) {
        this.ukviComplaintService = ukviComplaintService;
        this.ukviComplaintValidator = ukviComplaintValidator;
    }

    @SqsListener(value = "#{awsSqsConfig.ukviComplaint.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onAuditEvent(String message, @Header("MessageId") String messageId) throws Exception {
        ukviComplaintValidator.validate(message, messageId);
        ukviComplaintService.createComplaint(message, messageId);
    }

}
