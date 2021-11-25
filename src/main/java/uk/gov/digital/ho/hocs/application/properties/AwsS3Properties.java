package uk.gov.digital.ho.hocs.application.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.digital.ho.hocs.application.properties.model.Config;
import uk.gov.digital.ho.hocs.application.properties.model.S3Account;

import javax.validation.constraints.NotBlank;

@Configuration("awsS3Config")
@ConfigurationProperties(prefix = "aws.s3")
@Getter
@Setter
public class AwsS3Properties {

    private Config config;

    private Bucket untrusted;

    @Getter
    @Setter
    public static class Bucket {

        @NotBlank
        private String bucketName;

        private S3Account account;

    }
}
