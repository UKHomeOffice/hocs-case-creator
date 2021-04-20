package uk.gov.digital.ho.hocs.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class HealthMonitor {

    private final String healthFile;
    private final Integer shutdownWaitSeconds;

    public HealthMonitor(@Value("${case.creator.health-file}") String healthFile,
                         @Value("${case.creator.shutdown-delay-seconds}") Integer shutdownWaitSeconds) {
        this.healthFile = healthFile;
        this.shutdownWaitSeconds = shutdownWaitSeconds;
    }

    private void setHealthy() {
        File f = new File(healthFile);
        try {
            if (f.createNewFile()) {
                log.info("Health file {} created.", healthFile);
            } else {
                log.info("Failed to create Health file {}.", healthFile);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void setUnhealthy() {
        File f = new File(healthFile);
        if (f.delete()) {
            log.info("Health file {} deleted.", healthFile);
        } else {
            log.info("Failed to delete Health file {}.", healthFile);
        }
    }

    @PostConstruct
    public void onStarted() {
        setHealthy();
    }

    @PreDestroy
    public void onExit() {
        log.info("PreDestroy start");
        try {
            log.info("Sleep...");
            Thread.sleep(shutdownWaitSeconds * 1000);
            setUnhealthy();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        log.info("PreDestroy done");
    }
}
