package uk.gov.digital.ho.hocs.domain.queue.migration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.utilities.TestFileReader.getResourceFileAsString;

@ActiveProfiles(profiles = "local")
@RunWith(MockitoJUnitRunner.class)
public class MigrationMessageValidatorTest {

    private MigrationMessageValidator migrationMessageValidator;

    @Mock
    private MessageLogService messageLogService;

    private final String messageId = UUID.randomUUID().toString();

    @Before
    public void setUp() {
        migrationMessageValidator = new MigrationMessageValidator(
                new SpringConfiguration().objectMapper(),
                messageLogService);
    }

    @Test
    public void shouldValidateWithValidMigrationMessage() throws Exception {
        var validMessage = getResourceFileAsString("migration/validMigration.json");
        migrationMessageValidator.validate(messageId, validMessage);
    }

    @Test(expected = Exception.class)
    public void shouldNotValidateWithInvalidMigrationMessageWithMissingPrimaryCorrespondent() throws Exception {
        var invalidMessage = getResourceFileAsString("migration/invalidMigrationMissingPrimaryCorrespondent.json");
        migrationMessageValidator.validate(messageId, invalidMessage);
    }

    @Test(expected = Exception.class)
    public void shouldNotValidateWithInvalidMigrationMessageWithMissingRequiredFieldsForPrimaryCorrespondent() throws Exception {
        var invalidMessage = getResourceFileAsString("migration/invalidMigrationMissingRequiredFieldsPrimaryCorrespondent.json");
        migrationMessageValidator.validate(messageId, invalidMessage);
    }

    @Test(expected = Exception.class)
    public void shouldNotValidateWithInvalidMigrationMessageWithMissingRequiredFieldsForAdditionalCorrespondent() throws Exception {
        var invalidMessage = getResourceFileAsString("migration/invalidMigrationMissingRequiredFieldsAdditionalCorrespondent.json");
        migrationMessageValidator.validate(messageId, invalidMessage);
    }

    @Test
    public void shouldValidateWithNoAdditionalCorrespondents() throws Exception {
        var validMessage = getResourceFileAsString("migration/validMigrationNoAdditionalCorrespondents.json");
        migrationMessageValidator.validate(messageId, validMessage);
    }

    @Test(expected = Exception.class)
    public void shouldNotValidateWithInvalidMigrationMessageWithMissingCaseAttachments() throws Exception {
        var invalidMessage = getResourceFileAsString("migration/invalidMigrationMessageMissingCaseAttachments.json");
        migrationMessageValidator.validate(messageId, invalidMessage);
    }

    @Test
    public void shouldValidateWithNoCaseAttachments() throws Exception {
        var validMessage = getResourceFileAsString("migration/validMigrationNoCaseAttachments.json");
        migrationMessageValidator.validate(messageId, validMessage);
    }

    @Test
    public void shouldValidateWithOptionalFieldsNull() throws Exception {
        var validMessage = getResourceFileAsString("migration/validMigrationWithOptionalFieldsNull.json");
        migrationMessageValidator.validate(messageId, validMessage);
    }
}