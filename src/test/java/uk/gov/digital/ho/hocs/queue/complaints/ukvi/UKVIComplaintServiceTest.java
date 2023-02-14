package uk.gov.digital.ho.hocs.queue.complaints.ukvi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;
import uk.gov.digital.ho.hocs.queue.complaints.ComplaintService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

@RunWith(MockitoJUnitRunner.class)
public class UKVIComplaintServiceTest {

    @Mock
    ComplaintService complaintService;
    @Mock
    ClientContext clientContext;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    EnumMappingsRepository enumMappingsRepository;

    @Test
    public void shouldCreateComplaint() {
        UKVITypeData complaintTypeData = new UKVITypeData();
        UKVIComplaintService ukviComplaintService = new UKVIComplaintService(objectMapper, enumMappingsRepository, complaintService, clientContext, complaintTypeData, "user", "group", "team");
        String json = getResourceFileAsString("staffBehaviour.json");

        ukviComplaintService.createComplaint("messageId", json);

        verify(complaintService).createComplaint(any(UKVIComplaintData.class), eq(complaintTypeData));
    }

    @Test
    public void shouldCreateComplaintWithNoCorrespondent() {
        UKVITypeData complaintTypeData = new UKVITypeData();
        UKVIComplaintService ukviComplaintService = new UKVIComplaintService(objectMapper, enumMappingsRepository, complaintService, clientContext, complaintTypeData, "user", "group", "team");
        String json = getResourceFileAsString("existingNoCorrespondent.json");

        ukviComplaintService.createComplaint("messageId", json);

        verify(complaintService).createComplaint(any(UKVIComplaintData.class), eq(complaintTypeData));
    }

}
