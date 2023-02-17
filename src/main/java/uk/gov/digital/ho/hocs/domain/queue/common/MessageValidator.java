package uk.gov.digital.ho.hocs.domain.queue.common;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

import java.io.InputStream;
import java.util.Set;
@Slf4j
public abstract class MessageValidator {

    private final ObjectMapper objectMapper;

    private final MessageLogService messageLogService;

    private final JsonSchema schema;

    public MessageValidator(ObjectMapper objectMapper,
                            MessageLogService messageLogService,
                            String schemaName) {
        InputStream in = getClass().getResourceAsStream(schemaName);
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        schema = schemaFactory.getSchema(in);
        this.objectMapper = objectMapper;
        this.messageLogService = messageLogService;
    }

    public void validate(Message message) throws Exception {
        try {
            JsonNode json = objectMapper.readTree(message.message());
            Set<ValidationMessage> validationMessages = schema.validate(json);
            if (!validationMessages.isEmpty()) {
                for (ValidationMessage validationMessage : validationMessages) {
                    log.warn("MessageId : {}, {}", message.id(), validationMessage.getMessage());
                }
                messageLogService.updateMessageLogEntryStatus(message.id(), Status.MESSAGE_VALIDATION_FAILED);
                throw new Exception("Schema validation failed for messageId : " + message.id());
            }
        } catch (JsonParseException e) {
            log.error("Schema validation failed for messageId {}, Exception : {} ", message.id(), e.getMessage());
            messageLogService.updateMessageLogEntryStatus(message.id(), Status.MESSAGE_PARSE_FAILURE);
            throw e;
        }

        messageLogService.updateMessageLogEntryStatus(message.id(), Status.MESSAGE_VALIDATION_SUCCESS);
    }
}
