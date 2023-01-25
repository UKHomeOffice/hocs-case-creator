package uk.gov.digital.ho.hocs.queue.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.MessageLogRepository;
import uk.gov.digital.ho.hocs.domain.model.MessageLog;
import uk.gov.digital.ho.hocs.domain.model.Status;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class MigrationStateService {

    private final MessageLogRepository messageLogRepository;

    public MigrationStateService(MessageLogRepository messageLogRepository) {
        this.messageLogRepository = messageLogRepository;
    }

    public void createTestCase() {
        MessageLog state = new MessageLog(
                UUID.randomUUID(),
                "message_id",
                UUID.randomUUID(),
                "message: {}",
                Status.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        messageLogRepository.save(state);
    }

    public void createState(MessageLog state) {
        messageLogRepository.save(state);
    }
}