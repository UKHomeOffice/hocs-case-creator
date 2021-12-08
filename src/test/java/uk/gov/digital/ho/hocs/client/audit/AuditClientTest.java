package uk.gov.digital.ho.hocs.client.audit;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.client.audit.dto.EventType;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
public class AuditClientTest {

    @SpyBean
    private AmazonSNS amazonSNS;


    @Value("${aws.sns.audit.arn}")
    private String topicArn;

    private AuditClient auditClient;

    private final String raisingService = "case-creator";
    private final String namespace = "local";
    private final String userId = UUID.randomUUID().toString();
    private final String groups = "groupsString";
    private final String team = "teamString";
    private final String correlationId = UUID.randomUUID().toString();
    private final UUID caseId = UUID.randomUUID();
    private final UUID stageId = UUID.randomUUID();
    private final ClientContext clientContext = new ClientContext();

    @Captor
    ArgumentCaptor<PublishRequest> snsPublishedMessage;

    private ResultCaptor<PublishResult> snsPublishedResponse;

    @Before
    public void setUp() {
        snsPublishedResponse = new ResultCaptor<>();
        doAnswer(snsPublishedResponse).when(amazonSNS).publish(any());

        clientContext.setContext(userId, groups, team, correlationId);


        this.auditClient = new AuditClient(
                raisingService,
                namespace,
                topicArn,
                new SpringConfiguration().objectMapper(),
                clientContext,
                amazonSNS);
    }

    @Test
    public void shouldWriteAuditWithoutPayload() {
        auditClient.audit(EventType.CREATOR_CASE_CREATED, caseId, stageId);

        assertNotNull(snsPublishedResponse.getResult().getMessageId());
        verify(amazonSNS).publish(snsPublishedMessage.capture());
        ReadContext ctx = JsonPath.parse(snsPublishedMessage.getValue().getMessage());
        validateMainJsonBody(ctx);
        validateHeader(snsPublishedMessage.getValue().getMessageAttributes());
    }

    @Test
    public void shouldWriteAuditWithDataPayload() {
        Map<String, String> data = Map.of("key", "value");

        auditClient.audit(EventType.CREATOR_CASE_CREATED, caseId, stageId, data);

        assertNotNull(snsPublishedResponse.getResult().getMessageId());
        verify(amazonSNS).publish(snsPublishedMessage.capture());
        ReadContext ctx = JsonPath.parse(snsPublishedMessage.getValue().getMessage());
        validateMainJsonBody(ctx);
        assertEquals("{\"key\":\"value\"}", ctx.read("$.audit_payload"));
        validateHeader(snsPublishedMessage.getValue().getMessageAttributes());
    }

    @Test
    public void shouldWriteAuditWithJSONPayload() throws IOException {
        String jsonString = "{\"key\":\"value\"}";
        auditClient.audit(EventType.CREATOR_CASE_CREATED, caseId, stageId, jsonString);

        assertNotNull(snsPublishedResponse.getResult().getMessageId());
        verify(amazonSNS).publish(snsPublishedMessage.capture());
        ReadContext ctx = JsonPath.parse(snsPublishedMessage.getValue().getMessage());
        validateMainJsonBody(ctx);
        assertEquals("{\"key\":\"value\"}", ctx.read("$.audit_payload"));
        validateHeader(snsPublishedMessage.getValue().getMessageAttributes());
    }

    @Test(expected = IOException.class)
    public void shouldFailIfNOtJSONPayload() throws IOException {
        auditClient.audit(EventType.CREATOR_CASE_CREATED, caseId, stageId, "rubbish");
    }

    private void validateMainJsonBody(ReadContext ctx) {
        assertEquals(caseId.toString(), ctx.read("$.caseUUID"));
        assertEquals(stageId.toString(), ctx.read("$.stageUUID"));
        assertEquals(correlationId, ctx.read("$.correlation_id"));
        assertEquals(raisingService, ctx.read("$.raising_service"));
        assertEquals(namespace, ctx.read("$.namespace"));
        assertEquals(userId, ctx.read("$.user_id"));
    }

    private void validateHeader(Map<String, MessageAttributeValue> headerCaptor) {
        assertEquals(correlationId, headerCaptor.get(ClientContext.CORRELATION_ID_HEADER).getStringValue());
        assertEquals(groups, headerCaptor.get(ClientContext.GROUP_HEADER).getStringValue());
        assertEquals(userId, headerCaptor.get(ClientContext.USER_ID_HEADER).getStringValue());
    }

    /*
     * Used for returning the MessageID from the SNS to confirm message acceptance.
     */
    public static class ResultCaptor<T> implements Answer<T> {
        private T result = null;
        public T getResult() {
            return result;
        }

        @Override
        public T answer(InvocationOnMock invocationOnMock) throws Throwable {
            result = (T) invocationOnMock.callRealMethod();
            return result;
        }
    }

}
