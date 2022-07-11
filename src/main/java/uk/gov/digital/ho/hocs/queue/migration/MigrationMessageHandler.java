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

    public MigrationMessageHandler(
            @Value("${message.ignored-types}") List<String> ignoredMessageTypes,
            MigrationCaseService migrationCaseService
    ) {
        super(ignoredMessageTypes);
        this.migrationCaseService = migrationCaseService;
    }

    @Override
    public void handleMessage(String message, String messageId) throws Exception {
        log.info("Received new message MessageId : {}, {}", messageId, message);

        //TBD
        // Case Validator to validate the message
        //MigrationService Create Migration Case for message
    }

    @Override
    public MessageTypes getMessageType() {
        return MessageTypes.MIGRATED_CASES;
    }

}
