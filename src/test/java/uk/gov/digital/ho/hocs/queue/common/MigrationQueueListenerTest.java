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

import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles("migration-local")
public class MigrationQueueListenerTest {

    @MockBean
    private MigrationMessageHandler migrationMessageHandler;

    @Autowired
    private QueueListener queueListener;

    @Test
    public void messageTypeDoesNotMatch_doNothing() throws Exception {
        when(migrationMessageHandler.getMessageType()).thenReturn(MessageTypes.UNKNOWN);

        queueListener.onMigrationEvent("test", "test");

        verify(migrationMessageHandler).getMessageType();
        verifyNoMoreInteractions(migrationMessageHandler);
    }

    @Test
    public void whenMigrationMessageShouldBeIgnored() throws Exception {
        when(migrationMessageHandler.getMessageType()).thenReturn(MessageTypes.MIGRATED_CASES);
        when(migrationMessageHandler.shouldIgnoreMessage()).thenReturn(true);

        queueListener.onMigrationEvent("test", "test");

        verify(migrationMessageHandler).getMessageType();
        verify(migrationMessageHandler).shouldIgnoreMessage();
        verifyNoMoreInteractions(migrationMessageHandler);
    }

}
