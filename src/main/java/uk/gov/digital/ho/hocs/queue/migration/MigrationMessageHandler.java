package uk.gov.digital.ho.hocs.queue.migration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.queue.common.BaseMessageHandler;
import uk.gov.digital.ho.hocs.queue.common.MessageTypes;

import java.util.List;

@Service
public class MigrationMessageHandler extends BaseMessageHandler {

    private final MigrationCaseService migrationCaseService;
    private final MigrationCaseValidator migrationCaseValidator;

    public MigrationMessageHandler(
            @Value("${message.ignored-types}") List<String> ignoredMessageTypes,
            MigrationCaseService migrationCaseService,
            MigrationCaseValidator migrationCaseValidator
    ) {
        super(ignoredMessageTypes);
        this.migrationCaseService = migrationCaseService;
        this.migrationCaseValidator = migrationCaseValidator;
    }

    @Override
    public void handleMessage(String message, String messageId) throws Exception {
        migrationCaseValidator.validate(message, messageId);
        migrationCaseService.createComplaint(message, messageId);
    }

    @Override
    public MessageTypes getMessageType() {
        return MessageTypes.MIGRATED_CASES;
    }

}
