package uk.gov.digital.ho.hocs.client.audit;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.apache.camel.ProducerTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.aws.LocalStackConfiguration;
import uk.gov.digital.ho.hocs.aws.SNSTopicPrefix;
import uk.gov.digital.ho.hocs.client.audit.dto.EventType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AuditClientTest {

    private AuditClient auditClient;

    @Mock
    private ProducerTemplate producerTemplate;

    private final String topicName = "audit-topic";
    private String topic;
    private final String raisingService = "case-creator";
    private final String namespace = "local";
    private final String userId = UUID.randomUUID().toString();
    private final String groups = "groupsString";
    private final String correlationId = UUID.randomUUID().toString();
    private final UUID caseId = UUID.randomUUID();
    private final UUID stageId = UUID.randomUUID();
    private final ClientContext clientContext = new ClientContext();

    @Captor
    ArgumentCaptor<HashMap<String, Object>> headerCaptor;
    @Captor
    ArgumentCaptor<String> jsonMessage;

    @Before
    public void setUp() {
        SNSTopicPrefix topicPrefix = new LocalStackConfiguration().topicPrefix();
        AuditTopicBuilder auditTopicBuilder = new AuditTopicBuilder(topicPrefix, topicName);
        topic = auditTopicBuilder.getTopic();
        clientContext.setContext(userId, groups, correlationId);
        auditClient = new AuditClient(
                auditTopicBuilder,
                raisingService,
                namespace,
                new SpringConfiguration().objectMapper(),
                clientContext,
                producerTemplate);
    }

    @Test
    public void shouldWriteAuditWithoutPayload() {

        auditClient.audit(EventType.CREATOR_CASE_CREATED, caseId, stageId);
        verify(producerTemplate).sendBodyAndHeaders(eq(topic), jsonMessage.capture(), headerCaptor.capture());
        ReadContext ctx = JsonPath.parse(jsonMessage.getValue());

        validateMainJsonBody(ctx);

        validateHeader(headerCaptor);

    }

    @Test
    public void shouldWriteAuditWithDataPayload() {
        Map<String, String> data = Map.of("key", "value");
        auditClient.audit(EventType.CREATOR_CASE_CREATED, caseId, stageId, data);
        verify(producerTemplate).sendBodyAndHeaders(eq(topic), jsonMessage.capture(), headerCaptor.capture());
        ReadContext ctx = JsonPath.parse(jsonMessage.getValue());

        validateMainJsonBody(ctx);
        assertEquals("{\"key\":\"value\"}", ctx.read("$.audit_payload"));

        validateHeader(headerCaptor);
    }

    @Test
    public void shouldWriteAuditWithJSONPayload() throws IOException {
        String jsonString = "{\"key\":\"value\"}";
        auditClient.audit(EventType.CREATOR_CASE_CREATED, caseId, stageId, jsonString);
        verify(producerTemplate).sendBodyAndHeaders(eq(topic), jsonMessage.capture(), headerCaptor.capture());
        ReadContext ctx = JsonPath.parse(jsonMessage.getValue());

        validateMainJsonBody(ctx);
        assertEquals("{\"key\":\"value\"}", ctx.read("$.audit_payload"));

        validateHeader(headerCaptor);
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

    private void validateHeader(ArgumentCaptor<HashMap<String, Object>> headerCaptor) {
        Map<String, Object> headerCaptorValue = headerCaptor.getValue();

        assertEquals(correlationId, (String) headerCaptorValue.get(ClientContext.CORRELATION_ID_HEADER));
        assertEquals(groups, (String) headerCaptorValue.get(ClientContext.GROUP_HEADER));
        assertEquals(userId, (String) headerCaptorValue.get(ClientContext.USER_ID_HEADER));
    }


}