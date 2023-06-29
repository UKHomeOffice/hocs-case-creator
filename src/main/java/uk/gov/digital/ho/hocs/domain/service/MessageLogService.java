package uk.gov.digital.ho.hocs.domain.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;
import uk.gov.digital.ho.hocs.domain.repositories.MessageLogRepository;
import uk.gov.digital.ho.hocs.domain.repositories.entities.MessageLog;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.domain.repositories.entities.Status.*;

@Service
public class MessageLogService {

    private final MessageLogRepository messageLogRepository;

    private final MeterRegistry meterRegistry;

    private static final List<Status> FAILED_STATUS = List.of(
            CASE_DOCUMENT_FAILED,
            CASE_CREATION_FAILED,
            CASE_CORRESPONDENTS_FAILED,
            CASE_STAGE_RETRIEVAL_FAILED,
            CASE_USER_UPDATE_FAILED,
            CASE_TEAM_UPDATE_FAILED
    );

    public MessageLogService(MessageLogRepository messageLogRepository, MeterRegistry meterRegistry) {
        this.messageLogRepository = messageLogRepository;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        Gauge.builder("decs.message.failures.count", messageLogRepository, m->m.countByStatusIn(FAILED_STATUS))
                .description("DECS Number of Webform failures")
                .register(meterRegistry);
    }

    public void createEntry(String messageId, UUID externalReference, MessageType type, String message) {
        var messageLog =
                new MessageLog(messageId, externalReference, message, Status.PENDING, type);
        messageLogRepository.save(messageLog);
    }

    public void createEntry(String messageId, UUID externalReference, MessageType type, String message, Status status) {
        var messageLog =
                new MessageLog(messageId, externalReference, message, status, type);
        messageLogRepository.save(messageLog);
    }

    @Transactional
    public void updateCaseUuidAndStatus(String messageId, UUID caseUuid, Status status) {
        messageLogRepository.updateCaseUuidAndStatus(messageId, caseUuid, status);
    }

    @Transactional
    public void updateStatus(String messageId, Status status) {
        messageLogRepository.updateStatus(messageId, status);
    }

    @Transactional
    public void completeWithStatus(String messageId, Status status) {
        messageLogRepository.updateStatusAndCompleted(messageId, status);
    }

    @Transactional
    public void updateProcessedTime(String id) {
        messageLogRepository.updateProcessedTime(id, LocalDateTime.now());
    }

    @Transactional
    public void complete(String messageId) {
        messageLogRepository.updateStatusAndCompleted(messageId, Status.COMPLETED);
    }

    @Transactional(readOnly = true)
    public long getCountOfPendingMessagesBetweenDates(LocalDateTime from, @NotNull LocalDateTime to) {
        if (from == null) {
            return messageLogRepository.countByStatusAndReceivedBefore(Status.PENDING, to);
        }
        return messageLogRepository.countByStatusAndReceivedBetween(Status.PENDING, from, to);
    }

    @Transactional(readOnly = true)
    public List<Message> getPendingMessagesBetweenDates(LocalDateTime from, @NotNull LocalDateTime to) {
        var messageLogStream = from == null
                ? messageLogRepository.findByStatusAndReceivedBefore(Status.PENDING, to)
                : messageLogRepository.findByStatusAndReceivedBetween(Status.PENDING, from, to);

        return messageLogStream.map(Message::new).toList();
    }

}
