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
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;
import uk.gov.digital.ho.hocs.domain.queue.migration.MigrationMessageHandler;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

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
@ActiveProfiles({"local", "migration"})
public class MigrationQueueListenerTest {

    @MockBean
    private MigrationMessageHandler migrationMessageHandler;

    @MockBean
    private MessageLogService messageLogService;

    @Autowired
    private MigrationQueueListener migrationQueueListener;

    @Test
    public void messageTypeMatches_callHandler() throws Exception {
        when(migrationMessageHandler.getMessageType()).thenCallRealMethod();

        migrationQueueListener.onMessageReceived("test", "test", MessageType.MIGRATION, UUID.randomUUID());

        verify(messageLogService).createMessageLogEntry(eq("test"), any(UUID.class), eq("test"));
        verify(migrationMessageHandler).getMessageType();
        verify(migrationMessageHandler).handleMessage("test", "test");
        verify(messageLogService).completeMessageLogEntry("test");
        verifyNoMoreInteractions(migrationMessageHandler);
    }

    @Test
    public void messageTypeNull_callHandler() throws Exception {
        migrationQueueListener.onMessageReceived("test", "test", null, UUID.randomUUID());

        verify(messageLogService).createMessageLogEntry(eq("test"), any(UUID.class), eq("test"));
        verify(migrationMessageHandler, times(0)).getMessageType();
        verify(migrationMessageHandler).handleMessage("test", "test");
        verify(messageLogService).completeMessageLogEntry("test");
        verifyNoMoreInteractions(migrationMessageHandler);
    }

    @Test(expected = ApplicationExceptions.InvalidMessageTypeException.class)
    public void messageTypeNotMatch_callHandler() throws Exception {
        when(migrationMessageHandler.getMessageType()).thenCallRealMethod();

        migrationQueueListener.onMessageReceived("test", "test", MessageType.UKVI_COMPLAINTS, UUID.randomUUID());

        verify(messageLogService).createMessageLogEntry(eq("test"), any(UUID.class), eq("test"));
        verify(migrationMessageHandler).getMessageType();
        verify(messageLogService).updateMessageLogEntryStatus("test", Status.MESSAGE_TYPE_INVALID);
        verifyNoMoreInteractions(migrationMessageHandler);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void whenMessageShouldBeIgnored_doNothing() throws Exception {
        ReflectionTestUtils.setField(migrationQueueListener, "shouldIgnoreMessages", true);

        when(migrationMessageHandler.getMessageType()).thenReturn(MessageType.MIGRATION);

        migrationQueueListener.onMessageReceived("test", "test", null, null);

        verifyNoMoreInteractions(migrationMessageHandler);
    }

}
