package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.retry.annotation.EnableRetry;

import java.io.IOException;

import static uk.gov.digital.ho.hocs.application.HealthMonitor.setHealthy;
import static uk.gov.digital.ho.hocs.application.HealthMonitor.setUnhealthy;

@Slf4j
@SpringBootApplication
@EnableRetry
public class CaseCreatorApplication {

    public static void main(String[] args) {

        SpringApplication application = new SpringApplication(CaseCreatorApplication.class);

        application.addListeners((ApplicationListener<ApplicationStartedEvent>) event -> {
            log.info("ApplicationStartedEvent");
            setHealthy();
        });

        application.addListeners((ApplicationListener<ContextClosedEvent>) event -> {
            log.info("ContextClosedEvent");
            setUnhealthy();
        });

        application.run(args);

    }
}
