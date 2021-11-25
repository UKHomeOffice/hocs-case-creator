package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import uk.gov.digital.ho.hocs.application.properties.AwsS3Properties;

@Configuration
@Profile({"s3"})
public class S3Configuration {

    @Primary
    @Bean
    public static AmazonS3 s3Client(AwsS3Properties awsS3Properties) {
        var credentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(awsS3Properties.getUntrusted().getAccount().getAccessKey(),
                        awsS3Properties.getUntrusted().getAccount().getSecretKey()));

        return AmazonS3ClientBuilder.standard()
                .withRegion(awsS3Properties.getConfig().getRegion())
                .withCredentials(credentialsProvider)
                .build();
    }
}
