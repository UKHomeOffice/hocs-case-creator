package uk.gov.digital.ho.hocs.application.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.digital.ho.hocs.application.properties.model.Account;
import uk.gov.digital.ho.hocs.application.properties.model.Config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Configuration("awsSqsConfig")
@ConfigurationProperties(prefix = "aws.sqs")
@Getter
@Setter
public class AwsSqsProperties {

    private Config config;

    private Queue ukviComplaint;

    @Getter
    @Setter
    public static class Queue {

        @NotBlank
        private String url;

        private Account account;

        private Attributes attributes;

        @Getter
        @Setter
        public static class Attributes {

            @Min(1)
            @Max(10)
            @NotNull
            private int maxMessages;

            @Min(1)
            @Max(20)
            @NotNull
            private int waitTime;

        }

    }
}
