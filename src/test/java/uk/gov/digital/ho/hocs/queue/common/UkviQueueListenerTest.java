package uk.gov.digital.ho.hocs.queue.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.digital.ho.hocs.queue.complaints.ukvi.UKVIComplaintMessageHandler;

import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles({"local", "ukvi"})
public class UkviQueueListenerTest {

    @MockBean
    private UKVIComplaintMessageHandler ukviComplaintMessageHandler;

    @Autowired
    private UkviQueueListener ukviQueueListener;

    @Test
    public void messageTypeDoesNotMatch_doNothing() throws Exception {
        when(ukviComplaintMessageHandler.getMessageType()).thenReturn(MessageTypes.UNKNOWN);

        ukviQueueListener.onComplaintEvent("test", "test", null);

        verify(ukviComplaintMessageHandler).getMessageType();
        verifyNoMoreInteractions(ukviComplaintMessageHandler);
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void whenMessageShouldBeIgnored_doNothing() throws Exception {
        ReflectionTestUtils.setField(ukviQueueListener, "shouldIgnoreMessages", true);

        when(ukviComplaintMessageHandler.getMessageType()).thenReturn(MessageTypes.UKVI_COMPLAINTS);

        ukviQueueListener.onComplaintEvent("test", "test", null);

        verifyNoMoreInteractions(ukviComplaintMessageHandler);
    }

}
