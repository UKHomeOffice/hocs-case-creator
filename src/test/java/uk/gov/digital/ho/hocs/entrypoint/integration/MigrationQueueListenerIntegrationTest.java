package uk.gov.digital.ho.hocs.entrypoint.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.utilities.TestFileReader;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles({"local", "migration"})
public class MigrationQueueListenerIntegrationTest extends AwsSqsIntegrationTestBase {

    @Autowired
    public ObjectMapper objectMapper;

    // Please see HOCS-5556. This is not currently used as a test.
    @Test
    public void consumeMessageFromMigrationQueue() {
       String validMessage = TestFileReader.getResourceFileAsString("migration/validMigration.json");
       amazonSQSAsync.sendMessage(queueUrl, validMessage);
    }

    @Test
    public void consumeMessageFromMigrationQueueWithNoAdditionalCorrespondents() {
        String validMessage = TestFileReader.getResourceFileAsString("migration/validMigrationNoAdditionalCorrespondents.json");
        amazonSQSAsync.sendMessage(queueUrl, validMessage);
    }

    @Test
    public void consumeMessageFromMigrationQueueWithNoCaseAttachments() {
        String validMessage = TestFileReader.getResourceFileAsString("migration/validMigrationNoCaseAttachments.json");
        amazonSQSAsync.sendMessage(queueUrl, validMessage);
    }

    @Test
    public void consumeMessageFromMigrationQueueWithCaseAttachments() {
        String validMessage = TestFileReader.getResourceFileAsString("migration/validMigrationWithCaseAttachments.json");
        amazonSQSAsync.sendMessage(queueUrl, validMessage);
    }

    @Test
    public void consumeMessageFromMigrationQueueWithOptionalFieldsNull() {
        String validMessage = TestFileReader.getResourceFileAsString("migration/validMigrationWithOptionalFieldsNull.json");
        amazonSQSAsync.sendMessage(queueUrl, validMessage);
    }

    @Test
    public void consumeMessageFromMigrationQueueWithCOMPCaseType() {
        String validMessage = TestFileReader.getResourceFileAsString("migration/validMigrationCOMP.json");
        amazonSQSAsync.sendMessage(queueUrl, validMessage);
    }

    @Test
    public void consumeMessageFromMigrationQueueWithBFCaseType() {
        String validMessage = TestFileReader.getResourceFileAsString("migration/validMigrationBF.json");
        amazonSQSAsync.sendMessage(queueUrl, validMessage);
    }

    @Test
    public void consumeMessageFromMigrationQueueWithIEDETCaseType() {
        String validMessage = TestFileReader.getResourceFileAsString("migration/validMigrationIEDET.json");
        amazonSQSAsync.sendMessage(queueUrl, validMessage);
    }

    @Test
    public void consumeMessageFromMigrationQueueWithPOGRCaseType() {
        String validMessage = TestFileReader.getResourceFileAsString("migration/validMigrationPOGR.json");
        amazonSQSAsync.sendMessage(queueUrl, validMessage);
    }
}
