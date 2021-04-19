package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import uk.gov.digital.ho.hocs.application.HealthMonitor;

import static uk.gov.digital.ho.hocs.application.HealthMonitor.setUnhealthy;

@Slf4j
@SpringBootApplication
@EnableRetry
public class CaseCreatorApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(CaseCreatorApplication.class, args);
        } catch (Exception e) {
            log.error(e.getMessage());
            setUnhealthy();
        }
    }
}
