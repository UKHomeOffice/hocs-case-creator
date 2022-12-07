package uk.gov.digital.ho.hocs.queue.migration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.application.SpringConfiguration;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

@ActiveProfiles(profiles = "local")
@RunWith(MockitoJUnitRunner.class)
public class MigrationMessageValidatorTest {

    private MigrationMessageValidator migrationMessageValidator;

    private final String messageId = UUID.randomUUID().toString();

    @Before
    public void setUp() {
        migrationMessageValidator = new MigrationMessageValidator(
                new SpringConfiguration().objectMapper());
    }

    @Test
    public void shouldValidateWithValidMigrationMessage() throws Exception {
        var validMessage = getResourceFileAsString("validMigration.json");
        migrationMessageValidator.validate(validMessage, messageId);
    }

    @Test(expected = Exception.class)
    public void shouldNotValidateWithInvalidMigrationMessageWithMissingPrimaryCorrespondent() throws Exception {
        var invalidMessage = getResourceFileAsString("invalidMigrationMissingPrimaryCorrespondent.json");
        migrationMessageValidator.validate(invalidMessage, messageId);
    }

    @Test(expected = Exception.class)
    public void shouldNotValidateWithInvalidMigrationMessageWithMissingRequiredFieldsForPrimaryCorrespondent() throws Exception {
        var invalidMessage = getResourceFileAsString("invalidMigrationMissingRequiredFieldsPrimaryCorrespondent.json");
        migrationMessageValidator.validate(invalidMessage, messageId);
    }

    @Test(expected = Exception.class)
    public void shouldNotValidateWithInvalidMigrationMessageWithMissingRequiredFieldsForAdditionalCorrespondent() throws Exception {
        var invalidMessage = getResourceFileAsString("invalidMigrationMissingRequiredFieldsAdditionalCorrespondent.json");
        migrationMessageValidator.validate(invalidMessage, messageId);
    }

    @Test
    public void shouldValidateWithNoAdditionalCorrespondents() throws Exception {
        var validMessage = getResourceFileAsString("validMigrationNoAdditionalCorrespondents.json");
        migrationMessageValidator.validate(validMessage, messageId);
    }

    @Test(expected = Exception.class)
    public void shouldNotValidateWithInvalidMigrationMessageWithMissingCaseAttachments() throws Exception {
        var invalidMessage = getResourceFileAsString("invalidMigrationMessageMissingCaseAttachments.json");
        migrationMessageValidator.validate(invalidMessage, messageId);
    }

    @Test
    public void shouldValidateWithNoCaseAttachments() throws Exception {
        var validMessage = getResourceFileAsString("validMigrationNoCaseAttachments.json");
        migrationMessageValidator.validate(validMessage, messageId);
    }
}