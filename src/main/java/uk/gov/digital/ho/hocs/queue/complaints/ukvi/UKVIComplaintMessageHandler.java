package uk.gov.digital.ho.hocs.queue.complaints.ukvi;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.queue.common.MessageHandler;
import uk.gov.digital.ho.hocs.queue.common.MessageTypes;

@Service
public class UKVIComplaintMessageHandler implements MessageHandler {

    private final UKVIComplaintService ukviComplaintService;
    private final UKVIComplaintValidator ukviComplaintValidator;

    public UKVIComplaintMessageHandler(
            UKVIComplaintService ukviComplaintService,
            UKVIComplaintValidator ukviComplaintValidator
    ) {
        this.ukviComplaintService = ukviComplaintService;
        this.ukviComplaintValidator = ukviComplaintValidator;
    }

    @Override
    public void handleMessage(String messageId, String message) throws Exception {
        ukviComplaintValidator.validate(messageId, message);
        ukviComplaintService.createComplaint(messageId, message);
    }

    @Override
    public MessageTypes getMessageType() {
        return MessageTypes.UKVI_COMPLAINTS;
    }

}
