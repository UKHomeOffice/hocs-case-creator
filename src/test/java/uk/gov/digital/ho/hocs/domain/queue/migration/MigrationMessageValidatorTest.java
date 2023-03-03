package uk.gov.digital.ho.hocs.domain.queue.migration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.application.RequestData;
import uk.gov.digital.ho.hocs.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;
import uk.gov.digital.ho.hocs.domain.validation.MessageValidator;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.utilities.TestFileReader.getResourceFileAsString;

@ActiveProfiles({"local", "migration"})
@RunWith(MockitoJUnitRunner.class)
public class MigrationMessageValidatorTest {

    private MessageValidator migrationMessageValidator;

    @Mock
    private MessageLogService messageLogService;

    @Mock
    private RequestData requestData;

    private final String messageId = UUID.randomUUID().toString();

    @Before
    public void setUp() {
        migrationMessageValidator = new MessageValidator(
                new SpringConfiguration(requestData).objectMapper(),
                messageLogService,
                "/hocs-migration-schema.json");
    }

    @Test
    public void shouldValidateWithValidMigrationMessage() throws Exception {
        var validMessage = getResourceFileAsString("migration/validMigration.json");
        var message = new Message(messageId, validMessage, MessageType.MIGRATION);
        migrationMessageValidator.validate(message);
    }

    @Test(expected = Exception.class)
    public void shouldNotValidateWithInvalidMigrationMessageWithMissingPrimaryCorrespondent() throws Exception {
        var invalidMessage = getResourceFileAsString("migration/invalidMigrationMissingPrimaryCorrespondent.json");
        var message = new Message(messageId, invalidMessage, MessageType.MIGRATION);
        migrationMessageValidator.validate(message);
    }

    @Test(expected = Exception.class)
    public void shouldNotValidateWithInvalidMigrationMessageWithMissingRequiredFieldsForPrimaryCorrespondent() throws Exception {
        var invalidMessage = getResourceFileAsString("migration/invalidMigrationMissingRequiredFieldsPrimaryCorrespondent.json");
        var message = new Message(messageId, invalidMessage, MessageType.MIGRATION);
        migrationMessageValidator.validate(message);
    }

    @Test(expected = Exception.class)
    public void shouldNotValidateWithInvalidMigrationMessageWithMissingRequiredFieldsForAdditionalCorrespondent() throws Exception {
        var invalidMessage = getResourceFileAsString("migration/invalidMigrationMissingRequiredFieldsAdditionalCorrespondent.json");
        var message = new Message(messageId, invalidMessage, MessageType.MIGRATION);
        migrationMessageValidator.validate(message);
    }

    @Test
    public void shouldValidateWithNoAdditionalCorrespondents() throws Exception {
        var validMessage = getResourceFileAsString("migration/validMigrationNoAdditionalCorrespondents.json");
        var message = new Message(messageId, validMessage, MessageType.MIGRATION);
        migrationMessageValidator.validate(message);
    }

    @Test(expected = Exception.class)
    public void shouldNotValidateWithInvalidMigrationMessageWithMissingCaseAttachments() throws Exception {
        var invalidMessage = getResourceFileAsString("migration/invalidMigrationMessageMissingCaseAttachments.json");
        var message = new Message(messageId, invalidMessage, MessageType.MIGRATION);
        migrationMessageValidator.validate(message);
    }

    @Test
    public void shouldValidateWithNoCaseAttachments() throws Exception {
        var validMessage = getResourceFileAsString("migration/validMigrationNoCaseAttachments.json");
        var message = new Message(messageId, validMessage, MessageType.MIGRATION);
        migrationMessageValidator.validate(message);
    }

    @Test
    public void shouldValidateWithOptionalFieldsNull() throws Exception {
        var validMessage = getResourceFileAsString("migration/validMigrationWithOptionalFieldsNull.json");
        var message = new Message(messageId, validMessage, MessageType.MIGRATION);
        migrationMessageValidator.validate(message);
    }
}
