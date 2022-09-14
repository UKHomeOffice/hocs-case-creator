package uk.gov.digital.ho.hocs.queue.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.queue.common.BaseMessageHandler;
import uk.gov.digital.ho.hocs.queue.common.MessageTypes;

import java.util.List;

@Slf4j
@Service
public class MigrationMessageHandler extends BaseMessageHandler {

    private final MigrationCaseService migrationCaseService;

    private final MigrationMessageValidator migrationValidator;

    public MigrationMessageHandler(
            @Value("${message.ignored-types}") List<String> ignoredMessageTypes,
            MigrationCaseService migrationCaseService,
            MigrationMessageValidator migrationValidator
    ) {
        super(ignoredMessageTypes);
        this.migrationCaseService = migrationCaseService;
        this.migrationValidator = migrationValidator;
    }

    @Override
    public void handleMessage(String message, String messageId) throws Exception {
        log.info("Received new message MessageId : {}", messageId);
        migrationValidator.validate(message, messageId);
        migrationCaseService.createMigrationCase(message, messageId);
    }

    @Override
    public MessageTypes getMessageType() {
        return MessageTypes.MIGRATION;
    }

}
