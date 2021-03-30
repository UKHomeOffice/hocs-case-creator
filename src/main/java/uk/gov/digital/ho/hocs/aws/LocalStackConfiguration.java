package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local"})
public class LocalStackConfiguration {

    public static final String REGION = "eu-west-2";
    private final AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {

        @Override
        public AWSCredentials getCredentials() {
            return new BasicAWSCredentials("test", "test");
        }

        @Override
        public void refresh() {

        }
    };

    @Bean
    public SQSQueuePrefix queuePrefix() {
        return new SQSQueuePrefix("aws-sqs://");
    }

    @Bean("sqsClient")
    public AmazonSQS sqsClient() {
        String host = "http://localhost:4576/";
        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(host, REGION);
        return AmazonSQSClientBuilder.standard()
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                .withCredentials(awsCredentialsProvider)
                .withEndpointConfiguration(endpoint)
                .build();
    }

    @Bean
    public SNSTopicPrefix topicPrefix() {
        return new SNSTopicPrefix("aws-sns://");
    }

    @Bean("auditSnsClient")
    public AmazonSNS auditSnsClient() {
        String host = "http://localhost:4575/";
        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(host, REGION);
        return AmazonSNSClientBuilder.standard()
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                .withCredentials(awsCredentialsProvider)
                .withEndpointConfiguration(endpoint)
                .build();
    }

    @Bean("s3Client")
    public AmazonS3 s3Client() {
        String host = "http://localhost:4572/";
        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(host, REGION);
        return AmazonS3ClientBuilder.standard()
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                .withCredentials(awsCredentialsProvider)
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpoint)
                .disableChunkedEncoding()
                .build();
    }
}