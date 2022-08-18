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

    private final MigrationValidator migrationValidator;
    public MigrationMessageHandler(
            @Value("${message.ignored-types}") List<String> ignoredMessageTypes,
            MigrationCaseService migrationCaseService,
            MigrationValidator migrationValidator
    ) {
        super(ignoredMessageTypes);
        this.migrationCaseService = migrationCaseService;
        this.migrationValidator = migrationValidator;
    }

    @Override
    public void handleMessage(String message, String messageId) throws Exception {
        migrationValidator.validate(message, messageId);
        log.info("Received new message MessageId : {}, {}", messageId, message);
    }

    @Override
    public MessageTypes getMessageType() {
        return MessageTypes.MIGRATION;
    }

}
