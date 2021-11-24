package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.digital.ho.hocs.application.properties.AwsSqsProperties;

@Configuration
public class SqsMessageListenerContainerFactory {

    @Primary
    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSqs,
                                                                                       AwsSqsProperties awsSqsProperties) {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();

        factory.setAmazonSqs(amazonSqs);
        factory.setMaxNumberOfMessages(awsSqsProperties.getUkviComplaint().getAttributes().getMaxMessages());
        factory.setWaitTimeOut(awsSqsProperties.getUkviComplaint().getAttributes().getWaitTime());

        return factory;
    }

}
