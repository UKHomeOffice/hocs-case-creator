package uk.gov.digital.ho.hocs.queue.ukvi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.testutil.TestFileReader;

import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
@Profile("local")
public class UKVIComplaintQueueTest extends BaseAwsSqsIntegrationTest {

    @Autowired
    public ObjectMapper objectMapper;

    @Test
    public void consumeMessageFromQueue() {
        String validMessage = TestFileReader.getResourceFileAsString("agentCorrespondent.json");

        amazonSQSAsync.sendMessage(queueUrl, validMessage);

        await().until(() -> getNumberOfMessagesNotVisibleOnQueue() == 1);
    }

}
