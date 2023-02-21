package uk.gov.digital.ho.hocs.domain.queue.complaints.ukvi;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.LogEvent;
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageHandler;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;
import uk.gov.digital.ho.hocs.domain.validation.MessageValidator;

@Service
@Profile("ukvi")
public class UKVIComplaintMessageHandler implements MessageHandler {

    private final UKVIComplaintService ukviComplaintService;
    private final MessageValidator ukviComplaintValidator;
    private final MessageLogService messageLogService;

    public UKVIComplaintMessageHandler(
            UKVIComplaintService ukviComplaintService,
            MessageValidator ukviComplaintValidator,
            MessageLogService messageLogService
    ) {
        this.ukviComplaintService = ukviComplaintService;
        this.ukviComplaintValidator = ukviComplaintValidator;
        this.messageLogService = messageLogService;
    }

    @Override
    public void handleMessage(Message message) throws Exception {
        if (message.type() != null && message.type() != MessageType.UKVI_COMPLAINTS) {
            messageLogService.updateStatus(message.id(), Status.MESSAGE_TYPE_INVALID);
            throw new ApplicationExceptions.InvalidMessageTypeException(String.format("Invalid message type %s", message.id()), LogEvent.INVALID_MESSAGE_TYPE);
        }

        ukviComplaintValidator.validate(message);
        ukviComplaintService.createComplaint(message.message());

        messageLogService.complete(message.id());

    }

}
