package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@EnableSqs
@Configuration
@Profile({"sqs"})
public class SqsConfiguration {

    @Primary
    @Bean
    public AmazonSQSAsync sqsClient(@Value("${aws.sqs.case-creator.account.access-key}") String accessKey,
                                    @Value("${aws.sqs.case-creator.account.secret-key}") String secretkey,
                                    @Value("${aws.sqs.config.region}") String region) {
        var credentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(accessKey, secretkey));

        return AmazonSQSAsyncClientBuilder
                .standard()
                .withRegion(region)
                .withCredentials(credentialsProvider)
                .build();
    }

}
