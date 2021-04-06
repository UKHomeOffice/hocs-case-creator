package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

@Configuration
@Profile({"s3"})
public class S3Configuration {


    @Bean("s3Client")
    public static AmazonS3 s3Client(@Value("${document.s3.access-key}") String accessKey,
                                    @Value("${document.s3.secret-key}") String secretKey,
                                    @Value("${document.s3.region}") String region) {

        if (StringUtils.isEmpty(accessKey)) {
            throw new BeanCreationException("Failed to create S3 client bean. Need non-blank value for access key");
        }

        if (StringUtils.isEmpty(secretKey)) {
            throw new BeanCreationException("Failed to create S3 client bean. Need non-blank values for secret key");
        }

        if (StringUtils.isEmpty(region)) {
            throw new BeanCreationException("Failed to create S3 bean. Need non-blank values for region");
        }

        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withClientConfiguration(new ClientConfiguration())
                .build();
    }
}