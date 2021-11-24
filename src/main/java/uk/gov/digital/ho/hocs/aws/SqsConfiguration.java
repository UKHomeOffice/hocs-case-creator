package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import uk.gov.digital.ho.hocs.application.properties.AwsSqsProperties;

@EnableSqs
@Configuration
@Profile({"sqs"})
public class SqsConfiguration {

    @Primary
    @Bean
    public AmazonSQSAsync sqsClient(AwsSqsProperties awsSqsProperties) {
        var credentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(awsSqsProperties.getUkviComplaint().getAccount().getAccessKey(),
                        awsSqsProperties.getUkviComplaint().getAccount().getSecretKey()));

        return AmazonSQSAsyncClientBuilder
                .standard()
                .withRegion(awsSqsProperties.getConfig().getRegion())
                .withCredentials(credentialsProvider)
                .build();
    }

}
