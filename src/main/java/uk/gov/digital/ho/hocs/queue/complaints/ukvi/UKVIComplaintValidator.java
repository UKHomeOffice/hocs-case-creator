package uk.gov.digital.ho.hocs.queue.complaints.ukvi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.queue.common.MessageValidator;
import uk.gov.digital.ho.hocs.service.MessageLogService;

@Service
public class UKVIComplaintValidator extends MessageValidator {

    public UKVIComplaintValidator(ObjectMapper objectMapper, MessageLogService messageLogService) {
        super(objectMapper, messageLogService,"/cmsSchema.json");
    }

}
