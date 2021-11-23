package uk.gov.digital.ho.hocs.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

@Configuration
@ConfigurationProperties(prefix = "document")
@Getter
@Setter
public class DocumentCustomProperties {
    @NotBlank
    private String namespace;
    @NotBlank
    private S3CustomProperties s3;

    @Getter
    @Setter
    private static class S3CustomProperties {
        @NotBlank
        private String accessKey;
        @NotBlank
        private String secretKey;
        @NotBlank
        private String untrustedBucketName;
        @NotBlank
        private String untrustedBucketKMSKey;
    }
}
