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
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;
import uk.gov.digital.ho.hocs.domain.queue.migration.MigrationMessageHandler;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
    public void messageNotIgnored_addedToMessageLogAndProcessed() throws Exception {
        migrationQueueListener.onMessageReceived("test", "test", MessageType.MIGRATION, UUID.randomUUID());

        verify(messageLogService).createMessageLogEntry(eq("test"), any(UUID.class), eq(MessageType.MIGRATION), eq("test"));
        verify(migrationMessageHandler).handleMessage(refEq(new Message("test", "test", MessageType.MIGRATION)));
        verifyNoMoreInteractions(messageLogService);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void whenMessageShouldBeIgnored_doNothing() throws Exception {
        ReflectionTestUtils.setField(migrationQueueListener, "shouldIgnoreMessages", true);

        migrationQueueListener.onMessageReceived("test", "test", null, null);

        verify(messageLogService).createMessageLogEntry("test", null, null, "test", Status.IGNORED);
        verifyNoMoreInteractions(migrationMessageHandler);
    }

}
