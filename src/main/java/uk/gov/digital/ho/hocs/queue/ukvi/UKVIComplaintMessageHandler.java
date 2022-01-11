package uk.gov.digital.ho.hocs.queue.ukvi;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.queue.common.MessageHandler;

@Service
public class UKVIComplaintMessageHandler implements MessageHandler {

    private final UKVIComplaintService ukviComplaintService;
    private final UKVIComplaintValidator ukviComplaintValidator;

    public UKVIComplaintMessageHandler(UKVIComplaintService ukviComplaintService,
                                       UKVIComplaintValidator ukviComplaintValidator) {
        this.ukviComplaintService = ukviComplaintService;
        this.ukviComplaintValidator = ukviComplaintValidator;
    }

    @Override
    public void handleMessage(String message, String messageId) throws Exception {
        ukviComplaintValidator.validate(message, messageId);
        ukviComplaintService.createComplaint(message, messageId);
    }

}
