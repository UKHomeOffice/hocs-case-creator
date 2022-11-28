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

@SpringBootTest(webEnvironment = RANDOM_PORT, properties="case-creator.mode=migration")
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
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
}
