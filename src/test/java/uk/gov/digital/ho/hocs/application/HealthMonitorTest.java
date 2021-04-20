package uk.gov.digital.ho.hocs.application;

import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HealthMonitorTest {
    private static final String FILENAME = "./healthy";
    private static final Integer WAIT = 2;
    private final HealthMonitor healthMonitor = new HealthMonitor(FILENAME, WAIT);

    @Test
    public void shouldCreateFile() {
        healthMonitor.onStarted();
        File file = new File(FILENAME);
        assertTrue(file.exists());
    }

    @Test
    public void shouldDeleteFile() {
        healthMonitor.onStarted();
        healthMonitor.onExit();
        File file = new File(FILENAME);
        assertFalse(file.exists());
    }

    @After
    public void tearDown() {
        healthMonitor.onExit();
    }
}
