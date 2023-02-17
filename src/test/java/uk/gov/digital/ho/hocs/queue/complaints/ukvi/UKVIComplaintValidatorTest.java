package uk.gov.digital.ho.hocs.queue.complaints.ukvi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.service.MessageLogService;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

@ActiveProfiles(profiles = "local")
@RunWith(MockitoJUnitRunner.class)
public class UKVIComplaintValidatorTest {
    private UKVIComplaintValidator complaintValidator;

    @Mock
    private MessageLogService messageLogService;

    private final String messageId = UUID.randomUUID().toString();

    @Before
    public void setUp() {
        complaintValidator = new UKVIComplaintValidator(
                new SpringConfiguration().objectMapper(),
                messageLogService);
    }

    @Test
    public void shouldPassValidationWithGoodJson() throws Exception {
        var goodJson =  getResourceFileAsString("staffBehaviour.json");
        complaintValidator.validate(messageId, goodJson);
    }

    @Test(expected = Exception.class)
    public void shouldFailValidationWithBadJson() throws Exception {
        var badJson =  getResourceFileAsString("incorrect.json");
        complaintValidator.validate(messageId, badJson);
    }

    @Test(expected = Exception.class)
    public void shouldFailValidationWithNotJson() throws Exception {
        var notJson =  getResourceFileAsString("notJson.txt");
        complaintValidator.validate(messageId, notJson);
    }
}
