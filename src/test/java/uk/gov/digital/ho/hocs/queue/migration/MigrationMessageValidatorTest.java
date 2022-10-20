package uk.gov.digital.ho.hocs.queue.migration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.queue.complaints.ukvi.UKVIComplaintValidator;

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
    public void shouldValidateWithInvalidMigrationMessageWithMissingPrimaryCorrespondent() throws Exception {
        var validMessage = getResourceFileAsString("invalidMigrationMissingPrimaryCorrespondent.json");
        migrationMessageValidator.validate(validMessage, messageId);
    }

    @Test(expected = Exception.class)
    public void shouldValidateWithInvalidMigrationMessageWithMissingRequiredFieldsForPrimaryCorrespondent() throws Exception {
        var validMessage = getResourceFileAsString("invalidMigrationMissingRequiredFields.json");
        migrationMessageValidator.validate(validMessage, messageId);
    }
}
