package uk.gov.digital.ho.hocs.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

@Configuration
@ConfigurationProperties(prefix = "audit")
@Getter
@Setter
public class AuditCustomProperties {
    @NotBlank
    private String appName;
    @NotBlank
    private String namespace;
    @NotBlank
    private SNSCustomProperties sns;

    @Getter
    @Setter
    private static class SNSCustomProperties {
        @NotBlank
        private String accessKey;
        @NotBlank
        private String secretKey;
        @NotBlank
        private String topicName;
        @NotBlank
        private int retries;
        @NotBlank
        private int delay;
    }
}
