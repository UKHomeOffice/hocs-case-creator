package uk.gov.digital.ho.hocs.queue.ukvi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.queue.common.BaseMessageHandler;
import uk.gov.digital.ho.hocs.queue.common.MessageTypes;

import java.util.List;

@Service
@Slf4j
public class UKVIComplaintMessageHandler extends BaseMessageHandler {

    private final UKVIComplaintService ukviComplaintService;
    private final UKVIComplaintValidator ukviComplaintValidator;

    public UKVIComplaintMessageHandler(
            @Value("${messages.ignored-types}") List<String> ignoredMessageTypes,
            UKVIComplaintService ukviComplaintService,
            UKVIComplaintValidator ukviComplaintValidator) {
        super(ignoredMessageTypes);

        this.ukviComplaintService = ukviComplaintService;
        this.ukviComplaintValidator = ukviComplaintValidator;
    }

    @Override
    public void handleMessage(String message, String messageId) throws Exception {
        if (shouldIgnoreMessage()) {
            log.info("Message received but ignored. MessageID : {}", messageId);
        } else {
            ukviComplaintValidator.validate(message, messageId);
            ukviComplaintService.createComplaint(message, messageId);
        }
    }

    @Override
    public MessageTypes getMessageType() {
        return MessageTypes.UKVI_COMPLAINTS;
    }
}
