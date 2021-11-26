package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"s3"})
public class S3Configuration {

    @Primary
    @Bean
    public static AmazonS3 s3Client(@Value("${aws.s3.untrusted.account.access-key}") String accessKey,
                                    @Value("${aws.s3.untrusted.account.secret-key}") String secretkey,
                                    @Value("${aws.s3.config.region}") String region) {

        var credentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(accessKey,secretkey));

        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(credentialsProvider)
                .build();
    }

}
