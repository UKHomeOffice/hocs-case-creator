package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

@Configuration
@Profile({"sqs"})
public class SQSConfiguration {

    @Bean(name = "sqsClient")
    public AmazonSQS sqsClient(@Value("${case.creator.sqs.access-key}") String accessKey,
                               @Value("${case.creator.sqs.secret-key}") String secretKey,
                               @Value("${aws.sqs.region}") String region) {

        if (StringUtils.isEmpty(accessKey)) {
            throw new BeanCreationException("Failed to create SQS client bean. Need non-blank value for access key");
        }

        if (StringUtils.isEmpty(secretKey)) {
            throw new BeanCreationException("Failed to create SQS client bean. Need non-blank values for secret key");
        }

        if (StringUtils.isEmpty(region)) {
            throw new BeanCreationException("Failed to create SQS client bean. Need non-blank values for region: " + region);
        }

        return AmazonSQSClientBuilder.standard()
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