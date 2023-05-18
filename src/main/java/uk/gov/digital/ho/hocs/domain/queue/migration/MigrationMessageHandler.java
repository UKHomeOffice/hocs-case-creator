package uk.gov.digital.ho.hocs.domain.queue.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.LogEvent;
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageHandler;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;
import uk.gov.digital.ho.hocs.domain.validation.MessageValidator;

@Slf4j
@Service
@Profile("migration")
public class MigrationMessageHandler implements MessageHandler {

    private final MigrationCaseService migrationCaseService;

    private final MessageValidator migrationValidator;
    private final MessageLogService messageLogService;

    public MigrationMessageHandler(
            MigrationCaseService migrationCaseService,
            MessageValidator migrationValidator,
            MessageLogService messageLogService
    ) {
        this.migrationCaseService = migrationCaseService;
        this.migrationValidator = migrationValidator;
        this.messageLogService = messageLogService;
    }

    @Override
    public void handleMessage(Message message) throws Exception {
        if (message.type() != null && message.type() != MessageType.MIGRATION) {
            messageLogService.updateStatus(message.id(), Status.MESSAGE_TYPE_INVALID);
            throw new ApplicationExceptions.InvalidMessageTypeException(String.format("Invalid message type %s", message.id()), LogEvent.INVALID_MESSAGE_TYPE);
        }

        migrationValidator.validate(message);
        Status status = migrationCaseService.createMigrationCase(message);

        messageLogService.completeWithStatus(message.id(), status);
    }

}
