package uk.gov.digital.ho.hocs.queue.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.queue.common.MessageHandler;
import uk.gov.digital.ho.hocs.queue.common.MessageTypes;


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
        migrationCaseService.createMigrationCase(messageId, message);
    }

    @Override
    public MessageTypes getMessageType() {
        return MessageTypes.MIGRATION;
    }

}
