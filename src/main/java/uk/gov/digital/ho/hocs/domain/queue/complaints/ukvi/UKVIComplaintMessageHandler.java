package uk.gov.digital.ho.hocs.domain.queue.complaints.ukvi;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageHandler;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;

@Service
@Profile("ukvi")
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
        ukviComplaintService.createComplaint(message);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.UKVI_COMPLAINTS;
    }

}
