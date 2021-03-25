package uk.gov.digital.ho.hocs.queue.ukvi;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.client.audit.AuditClient;
import uk.gov.digital.ho.hocs.client.audit.dto.EventType;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

@Slf4j
@ActiveProfiles(profiles = "local")
@RunWith(MockitoJUnitRunner.class)
public class UKVIComplaintValidatorTest {

    private UKVIComplaintValidator complaintValidator;

    @Mock
    private AuditClient auditClient;

    private String goodJson;
    private String badJson;
    private final String messageId = UUID.randomUUID().toString();

    @Before
    public void setUp() throws Exception {
        complaintValidator = new UKVIComplaintValidator(
                new SpringConfiguration().objectMapper(),
                new UKVITypeData(),
                auditClient);
        goodJson = getResourceFileAsString("staffBehaviour.json");
        badJson = getResourceFileAsString("incorrect.json");
    }

    @Test
    public void shouldAuditValidationSuccess() throws Exception {
        complaintValidator.validate(goodJson, messageId);
        verify(auditClient).audit(EventType.UKVI_PAYLOAD_PASSED_VALIDATED, null, null, goodJson);
    }

    @Test
    public void shouldAuditValidationFail() {
        try {
            complaintValidator.validate(badJson, messageId);
        } catch (Exception e) {
            // ignore
        }
        verify(auditClient).audit(EventType.UKVI_PAYLOAD_FAILED_VALIDATED, null, null);
    }

    @Test(expected = Exception.class)
    public void shouldFailValidationWithBadJson() throws Exception {
        complaintValidator.validate(badJson, messageId);
    }
}