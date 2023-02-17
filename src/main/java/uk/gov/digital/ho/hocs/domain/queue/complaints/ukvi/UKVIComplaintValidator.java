package uk.gov.digital.ho.hocs.domain.queue.complaints.ukvi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageValidator;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

@Service
public class UKVIComplaintValidator extends MessageValidator {

    public UKVIComplaintValidator(ObjectMapper objectMapper, MessageLogService messageLogService) {
        super(objectMapper, messageLogService,"/cmsSchema.json");
    }

}
