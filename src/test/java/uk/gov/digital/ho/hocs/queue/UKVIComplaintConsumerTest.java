package uk.gov.digital.ho.hocs.queue;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class UKVIComplaintConsumerTest extends CamelTestSupport {

    private final String complaintQueue = "direct:complaint-queue";
    private final String dlq = "mock:complaint-queue-dlq";

    @Mock
    private UKVIComplaintService mockUKVIComplaintService;

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new UKVIComplaintConsumer(mockUKVIComplaintService, complaintQueue, dlq, 0, 0, 0);
    }

    @Test
    public void shouldAcceptValidJson() throws Exception {
        String json = getResourceFileAsString("staffBehaviour.json");
        template.sendBody(complaintQueue, json);
        verify(mockUKVIComplaintService, times(1)).createComplaint(json);
        verifyNoMoreInteractions(mockUKVIComplaintService);
    }

    @Test
    public void shouldRejectInvalidJson() throws Exception {
        String json = getResourceFileAsString("incorrect.json");
        getMockEndpoint(dlq).setExpectedCount(1);
        template.sendBody(complaintQueue, json);
        verify(mockUKVIComplaintService, never()).createComplaint(json);
        getMockEndpoint(dlq).assertIsSatisfied();
    }

    @Test
    public void shouldMoveToDLQIfServiceFails() throws Exception {
        String json = getResourceFileAsString("staffBehaviour.json");
        doThrow(Exception.class)
                .when(mockUKVIComplaintService).createComplaint(json);
        getMockEndpoint(dlq).setExpectedCount(1);
        template.sendBody(complaintQueue, json);
        getMockEndpoint(dlq).assertIsSatisfied();
    }

    private String getResourceFileAsString(String fileName) {
        InputStream is = getResourceFileAsInputStream(fileName);
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } else {
            throw new RuntimeException("resource not found");
        }
    }

    private InputStream getResourceFileAsInputStream(String fileName) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
    }
}