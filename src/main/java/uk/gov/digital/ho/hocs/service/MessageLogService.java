package uk.gov.digital.ho.hocs.service;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.model.MessageLog;
import uk.gov.digital.ho.hocs.domain.model.Status;
import uk.gov.digital.ho.hocs.domain.repositories.MessageLogRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MessageLogService {

    private final MessageLogRepository messageLogRepository;

    //Constructor that injects the MessageLogRepository
    public MessageLogService(MessageLogRepository messageLogRepository) {
        this.messageLogRepository = messageLogRepository;
    }

    //Method to create a message log entry
    public void createMessageLogEntry(String messageId, UUID externalReference, String message) {
        var messageLog =
                new MessageLog(messageId, externalReference, null, message, Status.PENDING, null, LocalDateTime.now());
        messageLogRepository.save(messageLog);
    }

    //Method to update a message log entry status
    public void updateMessageLogEntryStatus(String messageId, Status status) {
        messageLogRepository.updateStatus(messageId, status);
    }

    public void completeMessageLogEntry(String messageId) {
        messageLogRepository.updateStatusAndCompleted(messageId, Status.COMPLETED);
    }

}
