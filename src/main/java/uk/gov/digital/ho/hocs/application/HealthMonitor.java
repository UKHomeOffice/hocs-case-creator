package uk.gov.digital.ho.hocs.application;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public class HealthMonitor {

    private static final String healthFileName = getHealthName();
    public static final String DEFAULT_HEALTH_FILE_NAME = "./healthy";
    public static final String CASE_CREATOR_HEALTH_FILE = "CASE_CREATOR_HEALTH_FILE";

    public static void setHealthy() throws IOException {
        if (getHealthFile().createNewFile()) {
            log.info("Health file {} created.", healthFileName);
        } else {
            log.info("Failed to create Health file {}.", healthFileName);
        }
    }

    public static void setUnhealthy() {
        if (getHealthFile().delete()) {
            log.info("Health file {} deleted.", healthFileName);
        } else {
            log.info("Failed to delete Health file {}.", healthFileName);
        }
    }

    public static boolean isHealthy() {
        return getHealthFile().exists();
    }

    private static File getHealthFile() {
        return new File(healthFileName);
    }

    private static String getHealthName() {
        return Optional.ofNullable(System.getenv().get(CASE_CREATOR_HEALTH_FILE)).orElse(DEFAULT_HEALTH_FILE_NAME);
    }
}
