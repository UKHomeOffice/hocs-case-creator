package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.retry.annotation.EnableRetry;

import javax.annotation.PreDestroy;

import static uk.gov.digital.ho.hocs.application.HealthMonitor.setHealthy;
import static uk.gov.digital.ho.hocs.application.HealthMonitor.setUnhealthy;

@Slf4j
@SpringBootApplication
@EnableRetry
public class CaseCreatorApplication {

    public static final int WAIT_FOR_IN_FLIGHT_TO_FINISH = 30 * 1000;

    public static void main(String[] args) {

        SpringApplication application = new SpringApplication(CaseCreatorApplication.class);

        application.addListeners((ApplicationListener<ApplicationStartedEvent>) event -> {
            log.info("ApplicationStartedEvent");
            setHealthy();
        });

        application.run(args);
    }

    @PreDestroy
    public void onExit() {
        log.info("PreDestroy start");
        try {
            log.info("Sleep...");
            Thread.sleep(WAIT_FOR_IN_FLIGHT_TO_FINISH);
            setUnhealthy();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        log.info("PreDestroy done");
    }
}
