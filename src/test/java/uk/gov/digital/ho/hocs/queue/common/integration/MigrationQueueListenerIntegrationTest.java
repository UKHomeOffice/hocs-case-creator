package uk.gov.digital.ho.hocs.queue.common.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.testutil.TestFileReader;

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
       String validMessage = TestFileReader.getResourceFileAsString("validMigration.json");
       amazonSQSAsync.sendMessage(migrationQueueUrl, validMessage);
    }

    @Test
    public void consumeMessageFromMigrationQueueWithNoAdditionalCorrespondents() {
        String validMessage = TestFileReader.getResourceFileAsString("validMigrationNoAdditionalCorrespondents.json");
        amazonSQSAsync.sendMessage(migrationQueueUrl, validMessage);
    }

    @Test
    public void consumeMessageFromMigrationQueueWithNoCaseAttachments() {
        String validMessage = TestFileReader.getResourceFileAsString("validMigrationNoCaseAttachments.json");
        amazonSQSAsync.sendMessage(migrationQueueUrl, validMessage);
    }

    @Test
    public void consumeMessageFromMigrationQueueWithCaseAttachments() {
        String validMessage = TestFileReader.getResourceFileAsString("validMigrationWithCaseAttachments.json");
        amazonSQSAsync.sendMessage(migrationQueueUrl, validMessage);
    }

    @Test
    public void consumeMessageFromMigrationQueueWithOptionalFieldsNull() {
        String validMessage = TestFileReader.getResourceFileAsString("validMigrationWithOptionalFieldsNull.json");
        amazonSQSAsync.sendMessage(migrationQueueUrl, validMessage);
    }
}
