package uk.gov.digital.ho.hocs.domain.queue.complaints.ukvi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;
import uk.gov.digital.ho.hocs.domain.queue.complaints.ComplaintService;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.utilities.TestFileReader.getResourceFileAsString;

@RunWith(MockitoJUnitRunner.class)
public class UKVIComplaintServiceTest {

    @Mock
    private ComplaintService complaintService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private EnumMappingsRepository enumMappingsRepository;
    private String messageId;

    @Before
    public void setUp() {
        messageId = UUID.randomUUID().toString();
    }

    @Test
    public void shouldCreateComplaint() {
        UKVITypeData complaintTypeData = new UKVITypeData();
        UKVIComplaintService ukviComplaintService = new UKVIComplaintService(objectMapper, enumMappingsRepository, complaintService, complaintTypeData);
        Message message = new Message(messageId, getResourceFileAsString("webform/staffBehaviour.json"), MessageType.UKVI_COMPLAINTS);

        ukviComplaintService.createComplaint(message);

        verify(complaintService).createComplaint(eq(messageId), any(UKVIComplaintData.class), eq(complaintTypeData));
    }

    @Test
    public void shouldCreateComplaintWithNoCorrespondent() {
        UKVITypeData complaintTypeData = new UKVITypeData();
        UKVIComplaintService ukviComplaintService = new UKVIComplaintService(objectMapper, enumMappingsRepository, complaintService, complaintTypeData);
        Message message = new Message(messageId, getResourceFileAsString("webform/existingNoCorrespondent.json"), MessageType.UKVI_COMPLAINTS);

        ukviComplaintService.createComplaint(message);

        verify(complaintService).createComplaint(eq(messageId), any(UKVIComplaintData.class), eq(complaintTypeData));
    }

}
