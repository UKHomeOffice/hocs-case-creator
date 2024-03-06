package uk.gov.digital.ho.hocs.domain.queue.migration;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"local", "migration"})
public class CaseDataServiceTest {

    @Autowired
    private ObjectMapper objectMapper;

    private CaseDataService caseDataService;
    private ListAppender<ILoggingEvent> logMessages;

    @Before
    public void setup() {
        caseDataService = new CaseDataService(objectMapper);

        Logger exceptionLogger = (Logger) LoggerFactory.getLogger(CaseDataService.class);
        exceptionLogger.detachAndStopAllAppenders();

        logMessages = new ListAppender<>();
        logMessages.start();

        exceptionLogger.addAppender(logMessages);
    }

    @Test
    public void canParseCaseDataJson() {
        String messageId = UUID.randomUUID().toString();
        String validJson = "[" +
            "{\"name\": \"name1\", \"value\": \"value1\"}," +
            "{\"name\": \"name2\", \"value\": \"value2\"}" +
            "]";

        Map<String, String> caseData = caseDataService.parseCaseDataJson(messageId, validJson);

        Map<String, String> expected = Map.of(
            "name1", "value1",
            "name2", "value2"
        );

        assertThat(caseData).isEqualTo(expected);
    }

    @Test
    public void logsWarningAndReturnsEmptyMapOnInvalidJson() {
        String messageId = UUID.randomUUID().toString();
        String invalidJson = "{\"name1\": \"value1\"}";

        Map<String, String> caseData = caseDataService.parseCaseDataJson(messageId, invalidJson);

        String expectedFormattedMessageStartsWith =
            "Failed to parse case data for message %s: Cannot deserialize value".formatted(messageId);

        assertThat(caseData).isEqualTo(Map.of());

        assertThat(logMessages.list.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(logMessages.list.get(0).getFormattedMessage()).startsWith(expectedFormattedMessageStartsWith);
    }
}
