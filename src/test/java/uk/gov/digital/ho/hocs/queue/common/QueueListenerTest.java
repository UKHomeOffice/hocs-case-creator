package uk.gov.digital.ho.hocs.queue.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.queue.complaints.ukvi.UKVIComplaintMessageHandler;
import uk.gov.digital.ho.hocs.queue.migration.MigrationMessageHandler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
public class QueueListenerTest {

    @MockBean
    private UKVIComplaintMessageHandler ukviComplaintMessageHandler;

    @Autowired
    private QueueListener queueListener;

    @Test
    public void messageTypeDoesNotMatch_doNothing() throws Exception {
        when(ukviComplaintMessageHandler.getMessageType()).thenReturn(MessageTypes.UNKNOWN);

        queueListener.onComplaintEvent("test", "test");

        verify(ukviComplaintMessageHandler).getMessageType();
        verifyNoMoreInteractions(ukviComplaintMessageHandler);
    }

    @Test
    public void whenMessageShouldBeIgnored_doNothing() throws Exception {
        when(ukviComplaintMessageHandler.getMessageType()).thenReturn(MessageTypes.UKVI_COMPLAINTS);
        when(ukviComplaintMessageHandler.shouldIgnoreMessage()).thenReturn(true);

        queueListener.onComplaintEvent("test", "test");

        verify(ukviComplaintMessageHandler).getMessageType();
        verify(ukviComplaintMessageHandler).shouldIgnoreMessage();
        verifyNoMoreInteractions(ukviComplaintMessageHandler);
    }

}
