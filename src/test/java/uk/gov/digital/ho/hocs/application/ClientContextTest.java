package uk.gov.digital.ho.hocs.application;

import com.google.code.tempusfugit.concurrency.ConcurrentTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(ConcurrentTestRunner.class)
public class ClientContextTest {
    private ClientContext clientContext;

    @Before
    public void setUp() {
        clientContext = new ClientContext();
    }

    @Test
    public void ThreadOne() {
        clientContext.setContext("u1", "g1", "t1", "c1");
        assertEquals("u1", clientContext.getUserId());
        assertEquals("g1", clientContext.getGroups());
        assertEquals("c1", clientContext.getCorrelationId());
    }

    @Test
    public void ThreadTwo() {
        clientContext.setContext("u2", "g2", "t2", "c2");
        assertEquals("u2", clientContext.getUserId());
        assertEquals("g2", clientContext.getGroups());
        assertEquals("c2", clientContext.getCorrelationId());
    }

    @Test
    public void ThreadThree() {
        clientContext.setContext("u3", "g3", "t3", "c3");
        assertEquals("u3", clientContext.getUserId());
        assertEquals("g3", clientContext.getGroups());
        assertEquals("c3", clientContext.getCorrelationId());
    }
}
