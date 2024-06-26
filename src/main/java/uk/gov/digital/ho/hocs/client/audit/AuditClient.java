package uk.gov.digital.ho.hocs.client.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.client.audit.dto.CreateAuditRequest;
import uk.gov.digital.ho.hocs.client.audit.dto.EventType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.application.LogEvent.*;

@Slf4j
@Component
public class AuditClient {

    private final String auditTopic;
    private final String raisingService;
    private final String namespace;
    private final ObjectMapper objectMapper;
    private final ClientContext clientContext;
    private final ProducerTemplate producerTemplate;
    public static final String EVENT_TYPE_HEADER = "event_type";

    @Autowired
    public AuditClient(AuditTopicBuilder auditTopicBuilder,
                       @Value("${info.app.name}") String raisingService,
                       @Value("${audit.namespace}") String namespace,
                       ObjectMapper objectMapper, ClientContext clientContext,
                       ProducerTemplate producerTemplate) {
        this.auditTopic = auditTopicBuilder.getTopic();
        this.raisingService = raisingService;
        this.namespace = namespace;
        this.objectMapper = objectMapper;
        this.clientContext = clientContext;
        this.producerTemplate = producerTemplate;
    }

    @Retryable(maxAttemptsExpression = "${audit.sns.retries}", backoff = @Backoff(delayExpression = "${audit.sns.delay}"))
    public void audit(EventType eventType, UUID caseUUID, UUID stageUUID) {
        sendAuditMessage(eventType, caseUUID, stageUUID);
    }

    @Retryable(maxAttemptsExpression = "${audit.sns.retries}", backoff = @Backoff(delayExpression = "${audit.sns.delay}"))
    public void audit(EventType eventType, UUID caseUUID, UUID stageForCaseUUID, Map<String, String> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            sendAuditMessage(eventType, caseUUID, stageForCaseUUID, json);
        } catch (JsonProcessingException e) {
            log.error("Failed to marshall data: {} , {}", data, e.getMessage());
        }
    }

    @Retryable(maxAttemptsExpression = "${audit.sns.retries}", backoff = @Backoff(delayExpression = "${audit.sns.delay}"))
    public void audit(EventType eventType, UUID caseUUID, UUID stageForCaseUUID, String json) throws IOException {
        objectMapper.readTree(json);
        sendAuditMessage(eventType, caseUUID, stageForCaseUUID, json);
    }

    private void sendAuditMessage(EventType eventType, UUID caseUUID, UUID stageUUID) {
        sendAuditMessage(eventType, caseUUID, stageUUID, "{}");
    }

    private void sendAuditMessage(EventType eventType, UUID caseUUID, UUID stageUUID, String payload) {
        CreateAuditRequest request = new CreateAuditRequest(
                clientContext.getCorrelationId(),
                caseUUID,
                stageUUID,
                raisingService,
                payload,
                namespace,
                LocalDateTime.now(),
                eventType,
                clientContext.getUserId());

        try {
            Map<String, Object> queueHeaders = getQueueHeaders(eventType.toString());
            String jsonPayload = objectMapper.writeValueAsString(request);
            producerTemplate.sendBodyAndHeaders(auditTopic, jsonPayload, queueHeaders);
            log.info("Create audit of type {} for Case UUID: {}, correlationID: {}, UserID: {}, event: {}",
                    eventType, caseUUID, clientContext.getCorrelationId(), clientContext.getUserId(), value(EVENT, AUDIT_EVENT_CREATED));
        } catch (Exception e) {
            log.error("Failed to create audit event for case UUID {}, event {}, exception: {}", caseUUID, value(EVENT, AUDIT_FAILED), value(EXCEPTION, e));
        }
    }

    private Map<String, Object> getQueueHeaders(String eventType) {
        return Map.of(
                EVENT_TYPE_HEADER, eventType,
                ClientContext.CORRELATION_ID_HEADER, clientContext.getCorrelationId(),
                ClientContext.USER_ID_HEADER, clientContext.getUserId(),
                ClientContext.GROUP_HEADER, clientContext.getGroups());
    }

}
