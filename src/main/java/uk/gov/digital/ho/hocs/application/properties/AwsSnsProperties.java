package uk.gov.digital.ho.hocs.application.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.digital.ho.hocs.application.properties.model.AccountWithId;
import uk.gov.digital.ho.hocs.application.properties.model.Config;

import javax.validation.constraints.NotBlank;

@Configuration("awsSnsConfig")
@ConfigurationProperties(prefix = "aws.sns")
@Getter
@Setter
public class AwsSnsProperties {

    private Config config;

    private Audit audit;

    @Getter
    @Setter
    public static class Audit {

        @NotBlank
        private AccountWithId account;

        @NotBlank
        private String topicName;

        @NotBlank
        private String arn;

    }
}
