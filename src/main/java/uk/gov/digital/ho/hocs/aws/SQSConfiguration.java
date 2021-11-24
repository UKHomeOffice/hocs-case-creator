package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"sqs"})
public class SQSConfiguration {

    @Primary
    @Bean
    public AmazonSQSAsync sqsClient(@Value("${case.creator.sqs.access-key}") String accessKey,
                                    @Value("${case.creator.sqs.secret-key}") String secretKey,
                                    @Value("${aws.sqs.region}") String region) {
        return AmazonSQSAsyncClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withClientConfiguration(new ClientConfiguration())
                .build();
    }

    @Bean
    public SQSQueuePrefix queuePrefix(@Value("${aws.sqs.region}") String region,
                                      @Value("${aws.account.id}") String accountId) {
        return new SQSQueuePrefix("aws-sqs://arn:aws:sqs:" + region + ":" + accountId + ":");
    }
}
