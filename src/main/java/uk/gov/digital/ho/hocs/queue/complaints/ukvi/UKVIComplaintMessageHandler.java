package uk.gov.digital.ho.hocs.queue.complaints.ukvi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.queue.common.BaseMessageHandler;
import uk.gov.digital.ho.hocs.queue.common.MessageTypes;

import java.util.List;

@Service
public class UKVIComplaintMessageHandler extends BaseMessageHandler {

    private final UKVIComplaintService ukviComplaintService;
    private final UKVIComplaintValidator ukviComplaintValidator;

    public UKVIComplaintMessageHandler(
            @Value("${message.ignored-types}") List<String> ignoredMessageTypes,
            UKVIComplaintService ukviComplaintService,
            UKVIComplaintValidator ukviComplaintValidator
    ) {
        super(ignoredMessageTypes);
        this.ukviComplaintService = ukviComplaintService;
        this.ukviComplaintValidator = ukviComplaintValidator;
    }

    @Override
    public void handleMessage(String message, String messageId) throws Exception {
        ukviComplaintValidator.validate(message, messageId);
        ukviComplaintService.createComplaint(message, messageId);
    }

    @Override
    public MessageTypes getMessageType() {
        return MessageTypes.UKVI_COMPLAINTS;
    }

}
