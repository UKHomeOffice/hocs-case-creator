package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"sns"})
public class SnsConfiguration {

    @Primary
    @Bean
    public AmazonSNSAsync auditSnsClient(@Value("${aws.sns.audit.account.access-key}") String accessKey,
                                         @Value("${aws.sns.audit.account.secret-key}") String secretkey,
                                         @Value("${aws.sns.config.region}") String region) {
        var credentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(accessKey, secretkey));

        return AmazonSNSAsyncClientBuilder
                .standard()
                .withRegion(region)
                .withCredentials(credentialsProvider)
                .build();
    }

}
