package uk.gov.digital.ho.hocs.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SpringConfiguration {

    @Bean
    public ClientContext createThreadContext() {
        return new ClientContext();
    }

    @Bean
    public RestTemplate createRestTemplate() {
        return new RestTemplate();
    }
}