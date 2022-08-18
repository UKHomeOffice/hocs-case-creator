package uk.gov.digital.ho.hocs.queue.migration;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.queue.complaints.ukvi.UKVITypeData;

import java.io.InputStream;
import java.util.Set;

@Slf4j
@Service
public class MigrationValidator {

    private final ObjectMapper objectMapper;
    private final JsonSchema schema;

    @Autowired
    public MigrationValidator(ObjectMapper objectMapper) {
        InputStream in = getClass().getResourceAsStream("/hocs-migration-schema.json");
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        schema = schemaFactory.getSchema(in);
        this.objectMapper = objectMapper;
    }

    public void validate(String jsonBody, String messageId) throws Exception {
        try {
            JsonNode json = objectMapper.readTree(jsonBody);
            Set<ValidationMessage> validationMessages = schema.validate(json);
            if (!validationMessages.isEmpty()) {
                for (ValidationMessage validationMessage : validationMessages) {
                    log.warn("MessageId : {}, {}", messageId, validationMessage.getMessage());
                }
                throw new Exception("Schema validation failed for messageId : " + messageId);
            }
        } catch (JsonParseException e) {
            log.error("Schema validation failed for messageId {}, Exception : {} ", messageId, e.getMessage());
            throw e;
        }
    }
}
