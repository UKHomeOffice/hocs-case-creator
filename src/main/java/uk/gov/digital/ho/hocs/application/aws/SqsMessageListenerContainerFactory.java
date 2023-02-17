package uk.gov.digital.ho.hocs.application.aws;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class SqsMessageListenerContainerFactory {

    @Primary
    @Bean
    @Profile("sqs")
    public SimpleMessageListenerContainerFactory awsMessageListenerContainerFactory(AmazonSQSAsync amazonSqs,
                                                                                       @Value("${aws.sqs.case-creator.attributes.max-messages}") Integer maxMessages) {
        return createMessageFactory(amazonSqs, maxMessages, null);
    }

    @Primary
    @Bean
    @Profile("local")
    public SimpleMessageListenerContainerFactory localstackMessageListenerContainerFactory(AmazonSQSAsync amazonSqs,
                                                                                           @Value("${aws.sqs.case-creator.attributes.max-messages}") Integer maxMessages,
                                                                                           @Value("${aws.sqs.case-creator.attributes.wait-time}") Integer waitTime) {
        return createMessageFactory(amazonSqs, maxMessages, waitTime);
    }

    public SimpleMessageListenerContainerFactory createMessageFactory(AmazonSQSAsync amazonSqs,
                                                                      Integer maxMessages,
                                                                      Integer waitTime) {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();

        factory.setAmazonSqs(amazonSqs);

        if (maxMessages != null) {
            factory.setMaxNumberOfMessages(maxMessages);
        }
        if (waitTime != null) {
            factory.setWaitTimeOut(waitTime);
        }

        return factory;
    }

}
