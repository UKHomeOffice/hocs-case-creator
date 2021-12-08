package uk.gov.digital.ho.hocs.queue.ukvi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.queue.ukvi.UKVIComplaintService;
import uk.gov.digital.ho.hocs.queue.ukvi.UKVIComplaintValidator;
import uk.gov.digital.ho.hocs.testutil.TestFileReader;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
@Profile("local")
public class UKVIComplaintQueueTest extends BaseAwsSqsIntegrationTest {

    @MockBean
    public UKVIComplaintService ukviComplaintService;

    @MockBean
    public UKVIComplaintValidator ukviComplaintValidator;

    @Autowired
    public ObjectMapper objectMapper;

    @Test
    public void consumeMessageFromQueue() {
        String validMessage = TestFileReader.getResourceFileAsString("agentCorrespondent.json");

        var result = amazonSQSAsync.sendMessage(queueUrl, validMessage);

        await().until(() -> getNumberOfMessagesOnQueue() == 0);
        await().untilAsserted(() -> verify(ukviComplaintValidator).validate(validMessage, result.getMessageId()));
        await().untilAsserted(() -> verify(ukviComplaintService).createComplaint(validMessage, result.getMessageId()));
    }

    @Test
    public void consumeMessageFromQueue_exceptionMakesMessageNotVisible() throws Exception {
        String validMessage = TestFileReader.getResourceFileAsString("incorrect.json");

        doThrow(new NullPointerException("TEST")).when(ukviComplaintValidator).validate(eq(validMessage), any());

        var result = amazonSQSAsync.sendMessage(queueUrl, validMessage);

        await().until(() -> getNumberOfMessagesOnQueue() == 0);
        await().until(() -> getNumberOfMessagesNotVisibleOnQueue() == 1);

        await().untilAsserted(() -> verify(ukviComplaintValidator).validate(validMessage, result.getMessageId()));
        await().untilAsserted(() -> verifyNoMoreInteractions(ukviComplaintValidator));
        await().untilAsserted(() -> verifyNoMoreInteractions(ukviComplaintService));
    }

}
