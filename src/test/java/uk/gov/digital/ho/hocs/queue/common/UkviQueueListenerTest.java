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
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.model.Status;
import uk.gov.digital.ho.hocs.queue.complaints.ukvi.UKVIComplaintMessageHandler;
import uk.gov.digital.ho.hocs.service.MessageLogService;

import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
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
    public void messageTypeMatches_callHandler() throws Exception {
        when(ukviComplaintMessageHandler.getMessageType()).thenCallRealMethod();

        ukviQueueListener.onComplaintEvent("test", "test", MessageType.UKVI_COMPLAINTS, UUID.randomUUID());

        verify(messageLogService).createMessageLogEntry(eq("test"), any(UUID.class), eq("test"));
        verify(ukviComplaintMessageHandler).getMessageType();
        verify(ukviComplaintMessageHandler).handleMessage("test", "test");
        verify(messageLogService).completeMessageLogEntry(eq("test"));
        verifyNoMoreInteractions(ukviComplaintMessageHandler);
    }

    @Test
    public void messageTypeNull_callHandler() throws Exception {
        ukviQueueListener.onComplaintEvent("test", "test", null, UUID.randomUUID());

        verify(messageLogService).createMessageLogEntry(eq("test"), any(UUID.class), eq("test"));
        verify(ukviComplaintMessageHandler, times(0)).getMessageType();
        verify(ukviComplaintMessageHandler).handleMessage("test", "test");
        verify(messageLogService).completeMessageLogEntry(eq("test"));
        verifyNoMoreInteractions(ukviComplaintMessageHandler);
    }

    @Test(expected = ApplicationExceptions.InvalidMessageTypeException.class)
    public void messageTypeNotMatch_callHandler() throws Exception {
        when(ukviComplaintMessageHandler.getMessageType()).thenCallRealMethod();

        ukviQueueListener.onComplaintEvent("test", "test", MessageType.MIGRATION, UUID.randomUUID());

        verify(messageLogService).createMessageLogEntry(eq("test"), any(UUID.class), eq("test"));
        verify(ukviComplaintMessageHandler).getMessageType();
        verify(messageLogService).updateMessageLogEntryStatus(eq("test"), eq(Status.MESSAGE_TYPE_INVALID));
        verifyNoMoreInteractions(ukviComplaintMessageHandler);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void whenMessageShouldBeIgnored_doNothing() throws Exception {
        ReflectionTestUtils.setField(ukviQueueListener, "shouldIgnoreMessages", true);

        when(ukviComplaintMessageHandler.getMessageType()).thenReturn(MessageType.MIGRATION);

        ukviQueueListener.onComplaintEvent("test", "test", null, null);

        verifyNoMoreInteractions(ukviComplaintMessageHandler);
    }

}
