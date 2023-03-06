package uk.gov.digital.ho.hocs.domain.validation;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.application.LogEvent;
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

import java.io.InputStream;
import java.util.Set;

@Slf4j
public class MessageValidator {

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
                    log.warn("Message schema validation failed for message: {}. Reason: {}", message.id(), validationMessage.getMessage());
                }
                messageLogService.updateStatus(message.id(), Status.MESSAGE_VALIDATION_FAILED);
                throw new ApplicationExceptions.MessageSchemaValidationException(
                        String.format("Schema validation failed for messageId: %s.", message.id()),
                        LogEvent.MESSAGE_SCHEMA_VALIDATION_FAILURE);
            }
        } catch (JsonParseException e) {
            log.error("Message schema failed to parse for message: {}. Reason: {}", message.id(), e.getMessage());
            messageLogService.updateStatus(message.id(), Status.MESSAGE_PARSE_FAILURE);
            throw new ApplicationExceptions.MessageSchemaParseException(
                    String.format("Message schema failed to parse for message: %s. Reason: %s", message.id(), e.getMessage()),
                    LogEvent.MESSAGE_SCHEMA_PARSE_FAILURE);
        }

        messageLogService.updateStatus(message.id(), Status.MESSAGE_VALIDATION_SUCCESS);
    }
}
