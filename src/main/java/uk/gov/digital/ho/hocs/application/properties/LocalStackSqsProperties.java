package uk.gov.digital.ho.hocs.application.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.validation.constraints.NotBlank;

@Profile("local")
@Configuration
@ConfigurationProperties(prefix = "localstack.sqs")
@Getter
@Setter
public class LocalStackSqsProperties {

    private Config config;

    @Getter
    @Setter
    public static class Config {

        @NotBlank
        @Getter
        private String region;

        @NotBlank
        @Getter
        private String baseUrl;

    }
}
