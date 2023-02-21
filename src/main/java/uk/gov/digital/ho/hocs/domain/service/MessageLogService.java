package uk.gov.digital.ho.hocs.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.repositories.MessageLogRepository;
import uk.gov.digital.ho.hocs.domain.repositories.entities.MessageLog;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MessageLogService {

    private final MessageLogRepository messageLogRepository;

    public MessageLogService(MessageLogRepository messageLogRepository) {
        this.messageLogRepository = messageLogRepository;
    }

    public void createMessageLogEntry(String messageId, UUID externalReference, String message) {
        var messageLog =
                new MessageLog(messageId, externalReference, null, message, Status.PENDING, null, LocalDateTime.now());
        messageLogRepository.save(messageLog);
    }

    public void createMessageLogEntry(String messageId, UUID externalReference, String message, Status status) {
        var messageLog =
                new MessageLog(messageId, externalReference, null, message, status, null, LocalDateTime.now());
        messageLogRepository.save(messageLog);
    }

    @Transactional
    public void updateMessageLogEntryCaseUuidAndStatus(String messageId, UUID caseUuid, Status status) {
        messageLogRepository.updateCaseUuidAndStatus(messageId, caseUuid, status);
    }

    @Transactional
    public void updateMessageLogEntryStatus(String messageId, Status status) {
        messageLogRepository.updateStatus(messageId, status);
    }

    @Transactional
    public void completeMessageLogEntry(String messageId) {
        messageLogRepository.updateStatusAndCompleted(messageId, Status.COMPLETED);
    }


}
