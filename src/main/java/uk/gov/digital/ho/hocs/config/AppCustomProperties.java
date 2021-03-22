package uk.gov.digital.ho.hocs.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

@Configuration
@ConfigurationProperties(prefix = "case.creator")
@Getter
@Setter
public class AppCustomProperties {
    @NotBlank
    private String workflowService;
    @NotBlank
    private String caseService;
    @NotBlank
    private String basicAuth;
    @NotBlank
    private SQSCustomProperties sqs;
    @NotBlank
    private ClientCustomProperties ukviComplaint;

    @Getter
    @Setter
    private static class ClientCustomProperties {
        @NotBlank
        private String user;
        @NotBlank
        private String group;
        @NotBlank
        private String queueName;
        @NotBlank
        private String queue;
        @NotBlank
        private String dlQueueName;
        @NotBlank
        private String dlQueue;
        @NotBlank
        private Integer queueMaximumRedeliveries;
        @NotBlank
        private Integer queueRedeliveryDelay;
        @NotBlank
        private Integer queueBackOffMultiplier;
        @NotBlank
        private Integer queuePollDelay;
        @NotBlank
        private Integer queueWaitTimeSeconds;
        @NotBlank
        private Integer queueInitialDelay;
        @NotBlank
        private String redrivePolicy;
        @NotBlank
        private String queueProperties;
        @NotBlank
        private String dlQueueProperties;
    }

    @Getter
    @Setter
    private static class SQSCustomProperties {
        @NotBlank
        private String region;
        @NotBlank
        private String accountId;
        @NotBlank
        private String accessKey;
        @NotBlank
        private String secretKey;
    }
}
