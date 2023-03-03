package uk.gov.digital.ho.hocs.domain.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

@Configuration
public class ValidationConfiguration {

    @Bean
    @Profile("migration")
    public MessageValidator migrationMessageValidator(ObjectMapper objectMapper, MessageLogService messageLogService) {
        return new MessageValidator(objectMapper, messageLogService, "/hocs-migration-schema.json");
    }

    @Bean
    @Profile("ukvi")
    public MessageValidator ukviMessageValidator(ObjectMapper objectMapper, MessageLogService messageLogService) {
        return new MessageValidator(objectMapper, messageLogService, "/cmsSchema.json");
    }

}
