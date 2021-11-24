package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local"})
public class LocalStackConfiguration {

    private static final String REGION = "eu-west-2";
    private final AWSCredentialsProvider AWS_CREDENTIALS_PROVIDER
            = new AWSStaticCredentialsProvider(new AnonymousAWSCredentials());

    @Value("${aws.local-host:localhost}")
    private String awsHost;

    @Bean
    public SQSQueuePrefix queuePrefix() {
        return new SQSQueuePrefix("aws-sqs://");
    }

    @Primary
    @Bean
    public AmazonSQSAsync sqsClient() {
        String host = String.format("http://%s:4576/", awsHost);
        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(host, "eu-west-2");
        return AmazonSQSAsyncClientBuilder.standard()
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                .withCredentials(AWS_CREDENTIALS_PROVIDER)
                .withEndpointConfiguration(endpoint)
                .build();
    }

    @Bean
    public SNSTopicPrefix topicPrefix() {
        return new SNSTopicPrefix("aws-sns://");
    }

    @Bean("auditSnsClient")
    public AmazonSNS auditSnsClient() {
        String host = String.format("http://%s:4575/", awsHost);
        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(host, REGION);
        return AmazonSNSClientBuilder.standard()
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                .withCredentials(AWS_CREDENTIALS_PROVIDER)
                .withEndpointConfiguration(endpoint)
                .build();
    }

    @Bean("s3Client")
    public AmazonS3 s3Client() {
        String host = String.format("http://%s:4572/", awsHost);
        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(host, REGION);
        return AmazonS3ClientBuilder.standard()
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                .withCredentials(AWS_CREDENTIALS_PROVIDER)
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpoint)
                .disableChunkedEncoding()
                .build();
    }
}
