package uk.gov.digital.ho.hocs.queue.common.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.testutil.TestFileReader;

import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles({"local", "ukvi"})
public class UkviQueueListenerIntegrationTest extends AwsSqsIntegrationTestBase {

    @Autowired
    public ObjectMapper objectMapper;

    @Test
    public void consumeMessageFromQueue() {
        String validMessage = TestFileReader.getResourceFileAsString("agentCorrespondent.json");

        amazonSQSAsync.sendMessage(queueUrl, validMessage);

        await().until(() -> getNumberOfMessagesNotVisibleOnQueue() == 1);
    }
}
