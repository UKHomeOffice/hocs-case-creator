package uk.gov.digital.ho.hocs.entrypoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;
import uk.gov.digital.ho.hocs.domain.queue.complaints.ukvi.UKVIComplaintMessageHandler;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles({"local", "ukvi"})
public class UkviQueueListenerTest {

    @MockBean
    private UKVIComplaintMessageHandler ukviComplaintMessageHandler;

    @MockBean
    private MessageLogService messageLogService;

    @Autowired
    private UkviQueueListener ukviQueueListener;

    @Test
    public void messageNotIgnored_addedToMessageLog() {
        ukviQueueListener.onMessageReceived("test", "test", MessageType.UKVI_COMPLAINTS, UUID.randomUUID());

        verify(messageLogService).createMessageLogEntry(eq("test"), any(UUID.class), eq(MessageType.UKVI_COMPLAINTS), eq("test"));
        verifyNoMoreInteractions(messageLogService);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void whenMessageShouldBeIgnored_doNothing() {
        ReflectionTestUtils.setField(ukviQueueListener, "shouldIgnoreMessages", true);

        ukviQueueListener.onMessageReceived("test", "test", null, null);

        verify(messageLogService).createMessageLogEntry("test", null, null, "test", Status.IGNORED);
        verifyNoMoreInteractions(ukviComplaintMessageHandler);
    }

}
