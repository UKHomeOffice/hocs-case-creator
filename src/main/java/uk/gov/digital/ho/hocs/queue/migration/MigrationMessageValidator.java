package uk.gov.digital.ho.hocs.queue.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.queue.common.MessageValidator;

@Service
public class MigrationMessageValidator extends MessageValidator {

    public MigrationMessageValidator(ObjectMapper objectMapper) {
        super(objectMapper, "/hocs-migration-schema.json");
    }


}
