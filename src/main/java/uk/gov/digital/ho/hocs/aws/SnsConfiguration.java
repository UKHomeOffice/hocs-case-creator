package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import uk.gov.digital.ho.hocs.application.properties.AwsSnsProperties;

@Configuration
@Profile({"sns"})
public class SnsConfiguration {

    @Primary
    @Bean
    public AmazonSNSAsync auditSnsClient(AwsSnsProperties awsSnsProperties) {
        var credentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(awsSnsProperties.getAudit().getAccount().getAccessKey(),
                        awsSnsProperties.getAudit().getAccount().getSecretKey()));

        return AmazonSNSAsyncClientBuilder
                .standard()
                .withRegion(awsSnsProperties.getConfig().getRegion())
                .withCredentials(credentialsProvider)
                .build();
    }

}
