package uk.gov.digital.ho.hocs.domain.queue.complaints.ukvi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.application.RequestData;
import uk.gov.digital.ho.hocs.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;
import uk.gov.digital.ho.hocs.domain.validation.MessageValidator;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.utilities.TestFileReader.getResourceFileAsString;

@ActiveProfiles({"local", "ukvi"})
@RunWith(MockitoJUnitRunner.class)
public class UKVIComplaintValidatorTest {

    private final String messageId = UUID.randomUUID().toString();
    private MessageValidator complaintValidator;
    @Mock
    private RequestData requestData;
    @Mock
    private MessageLogService messageLogService;

    @Before
    public void setUp() {
        complaintValidator = new MessageValidator(
                new SpringConfiguration(requestData).objectMapper(),
                messageLogService,
                "/cmsSchema.json");
    }

    @Test
    public void shouldPassValidationWithGoodJson() throws Exception {
        var goodJson = getResourceFileAsString("webform/staffBehaviour.json");
        var message = new Message(messageId, goodJson, MessageType.UKVI_COMPLAINTS);
        complaintValidator.validate(message);
    }

    @Test(expected = Exception.class)
    public void shouldFailValidationWithBadJson() throws Exception {
        var badJson = getResourceFileAsString("webform/incorrect.json");
        var message = new Message(messageId, badJson, MessageType.UKVI_COMPLAINTS);
        complaintValidator.validate(message);
    }

    @Test(expected = Exception.class)
    public void shouldFailValidationWithNotJson() throws Exception {
        var notJson = getResourceFileAsString("webform/notJson.txt");
        var message = new Message(messageId, notJson, MessageType.UKVI_COMPLAINTS);
        complaintValidator.validate(message);
    }
}
