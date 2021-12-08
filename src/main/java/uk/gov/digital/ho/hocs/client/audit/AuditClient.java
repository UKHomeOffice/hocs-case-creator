package uk.gov.digital.ho.hocs.client.audit;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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

    private final String raisingService;
    private final String namespace;
    private final ObjectMapper objectMapper;
    private final ClientContext clientContext;
    public static final String EVENT_TYPE_HEADER = "event_type";
    private final AmazonSNS snsClient;
    private final String topicArn;

    @Autowired
    public AuditClient(@Value("${info.app.name}") String raisingService,
                       @Value("${info.namespace}") String namespace,
                       @Value("${aws.sns.audit.arn}") String topicArn,
                       ObjectMapper objectMapper, ClientContext clientContext,
                       AmazonSNS snsClient) {
        this.raisingService = raisingService;
        this.namespace = namespace;
        this.objectMapper = objectMapper;
        this.clientContext = clientContext;
        this.snsClient = snsClient;
        this.topicArn = topicArn;
    }

    public void audit(EventType eventType, UUID caseUUID, UUID stageUUID) {
        sendAuditMessage(eventType, caseUUID, stageUUID);
    }

    public void audit(EventType eventType, UUID caseUUID, UUID stageForCaseUUID, Map<String, String> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            sendAuditMessage(eventType, caseUUID, stageForCaseUUID, json);
        } catch (JsonProcessingException e) {
            log.error("Failed to marshall data: {} , {}", data, e.getMessage());
        }
    }

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
            String jsonPayload = objectMapper.writeValueAsString(request);

            var publishRequest = new PublishRequest()
                    .withTopicArn(topicArn)
                    .withMessage(jsonPayload)
                    .withMessageAttributes(getQueueHeaders(eventType.toString()));

            snsClient.publish(publishRequest);

            log.info("Create audit of type {} for Case UUID: {}, correlationID: {}, UserID: {}, event: {}",
                    eventType, caseUUID, clientContext.getCorrelationId(), clientContext.getUserId(), value(EVENT, AUDIT_EVENT_CREATED));
        } catch (Exception e) {
            log.error("Failed to create audit event for case UUID {}, event {}, exception: {}", caseUUID, value(EVENT, AUDIT_FAILED), value(EXCEPTION, e));
        }
    }

    private Map<String, MessageAttributeValue> getQueueHeaders(String eventType) {
        return Map.of(
                EVENT_TYPE_HEADER, new StringMessageAttributeValue(eventType),
                ClientContext.CORRELATION_ID_HEADER, new StringMessageAttributeValue(clientContext.getCorrelationId()),
                ClientContext.USER_ID_HEADER, new StringMessageAttributeValue(clientContext.getUserId()),
                ClientContext.GROUP_HEADER, new StringMessageAttributeValue(clientContext.getGroups()));
    }

    private static class StringMessageAttributeValue extends MessageAttributeValue {
        private static final String type = "String";

        public StringMessageAttributeValue(String value) {
            if (!StringUtils.hasText(value)) {
                throw new IllegalArgumentException("Value should be a non-empty String");
            }

            this.setDataType(type);
            this.setStringValue(value);
        }
    }

}
