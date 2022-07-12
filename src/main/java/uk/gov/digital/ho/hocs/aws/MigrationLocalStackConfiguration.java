package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
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
@Profile({"migration"})
public class MigrationLocalStackConfiguration {

    private final AWSCredentialsProvider awsCredentialsProvider;
    private final AwsClientBuilder.EndpointConfiguration endpoint;

    public MigrationLocalStackConfiguration(@Value("${localstack.base-url}") String baseUrl,
                                            @Value("${localstack.config.region}") String region) {
        awsCredentialsProvider = new AWSStaticCredentialsProvider(new AnonymousAWSCredentials());
        endpoint = new AwsClientBuilder.EndpointConfiguration(baseUrl, region);
    }

}
