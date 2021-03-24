package uk.gov.digital.ho.hocs.queue;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

@Slf4j
@ActiveProfiles(profiles = "local")
@RunWith(MockitoJUnitRunner.class)
public class UKVIComplaintConsumerTest extends CamelTestSupport {

    private final String complaintQueue = "direct:complaint-queue";
    private final String dlq = "mock:complaint-queue-dlq";

    @Mock
    private UKVIComplaintService mockUKVIComplaintService;
    @Mock
    private UKVIComplaintQueueDetails queueDetails;

    @Override
    protected RouteBuilder createRouteBuilder() {
        when(queueDetails.getDlq()).thenReturn(dlq);
        when(queueDetails.getUkviComplaintQueue()).thenReturn(complaintQueue);
        return new UKVIComplaintConsumer(mockUKVIComplaintService, queueDetails);
    }

    @Test
    public void shouldAcceptValidJson() {
        String json = getResourceFileAsString("staffBehaviour.json");
        template.sendBody(complaintQueue, json);
        verify(mockUKVIComplaintService, times(1)).createComplaint(eq(json), any());
        verifyNoMoreInteractions(mockUKVIComplaintService);
    }

    @Test
    public void shouldRejectInvalidJson() throws Exception {
        String json = getResourceFileAsString("incorrect.json");
        getMockEndpoint(dlq).setExpectedCount(1);
        template.sendBody(complaintQueue, json);
        verify(mockUKVIComplaintService, never()).createComplaint(eq(json), any());
        getMockEndpoint(dlq).assertIsSatisfied();
    }

    @Test
    public void shouldMoveToDLQIfDownstreamServiceCallFails() throws RestClientException {
        String json = getResourceFileAsString("staffBehaviour.json");
        doThrow(RestClientException.class)
                .when(mockUKVIComplaintService).createComplaint(eq(json), any());
        getMockEndpoint(dlq).setExpectedCount(1);
        template.sendBody(complaintQueue, json);
    }
}
