package uk.gov.digital.ho.hocs.queue.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.queue.common.MessageValidator;
import uk.gov.digital.ho.hocs.service.MessageLogService;

@Service
public class MigrationMessageValidator extends MessageValidator {

    public MigrationMessageValidator(ObjectMapper objectMapper, MessageLogService messageLogService) {
        super(objectMapper, messageLogService, "/hocs-migration-schema.json");
    }


}
