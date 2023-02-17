package uk.gov.digital.ho.hocs.domain.queue.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageHandler;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;


@Slf4j
@Service
public class MigrationMessageHandler implements MessageHandler {

    private final MigrationCaseService migrationCaseService;

    private final MigrationMessageValidator migrationValidator;

    public MigrationMessageHandler(
            MigrationCaseService migrationCaseService,
            MigrationMessageValidator migrationValidator
    ) {
        this.migrationCaseService = migrationCaseService;
        this.migrationValidator = migrationValidator;
    }

    @Override
    public void handleMessage(String messageId, String message) throws Exception {
        log.info("Received new message MessageId : {}", messageId);
        migrationValidator.validate(messageId, message);
        migrationCaseService.createMigrationCase(message);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.MIGRATION;
    }

}
