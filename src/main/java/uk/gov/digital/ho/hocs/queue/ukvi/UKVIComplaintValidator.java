package uk.gov.digital.ho.hocs.queue.ukvi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.client.audit.AuditClient;

import java.io.InputStream;
import java.util.Set;

@Slf4j
@Service
public class UKVIComplaintValidator {

    private final ObjectMapper objectMapper;
    private final JsonSchema schema;
    private final UKVITypeData ukviTypeData;
    private final AuditClient auditClient;
    private final ClientContext clientContext;
    private final String user;
    private final String group;

    @Autowired
    public UKVIComplaintValidator(ObjectMapper objectMapper,
                                  UKVITypeData ukviTypeData,
                                  AuditClient auditClient,
                                  ClientContext clientContext,
                                  @Value("${case.creator.ukvi-complaint.user}") String user,
                                  @Value("${case.creator.ukvi-complaint.group}") String group) {
        this.ukviTypeData = ukviTypeData;
        this.auditClient = auditClient;
        this.clientContext = clientContext;
        this.user = user;
        this.group = group;
        InputStream in = getClass().getResourceAsStream("/cmsSchema.json");
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        schema = schemaFactory.getSchema(in);
        this.objectMapper = objectMapper;
    }

    public void validate(String jsonBody, String messageId) throws Exception {
        clientContext.setContext(user, group, "TODO", messageId);
        JsonNode json = objectMapper.readTree(jsonBody);
        Set<ValidationMessage> validationMessages = schema.validate(json);
        if (!validationMessages.isEmpty()) {
            for (ValidationMessage validationMessage : validationMessages) {
                log.warn("MessageId : {}, {}", messageId, validationMessage.getMessage());
            }
            auditClient.audit(ukviTypeData.getUnsuccessfulValidationEvent(), null, null);
            throw new Exception("Schema validation failed for messageId : " + messageId);
        }
    }
}
