package uk.gov.digital.ho.hocs.domain.queue.complaints.ukvi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.domain.queue.complaints.ComplaintService;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.utilities.TestFileReader.getResourceFileAsString;

@RunWith(MockitoJUnitRunner.class)
public class UKVIComplaintServiceTest {

    @Mock
    ComplaintService complaintService;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    EnumMappingsRepository enumMappingsRepository;

    @Test
    public void shouldCreateComplaint() {
        UKVITypeData complaintTypeData = new UKVITypeData();
        UKVIComplaintService ukviComplaintService = new UKVIComplaintService(objectMapper, enumMappingsRepository, complaintService, complaintTypeData);
        String json = getResourceFileAsString("webform/staffBehaviour.json");

        ukviComplaintService.createComplaint(json);

        verify(complaintService).createComplaint(any(UKVIComplaintData.class), eq(complaintTypeData));
    }

    @Test
    public void shouldCreateComplaintWithNoCorrespondent() {
        UKVITypeData complaintTypeData = new UKVITypeData();
        UKVIComplaintService ukviComplaintService = new UKVIComplaintService(objectMapper, enumMappingsRepository, complaintService, complaintTypeData);
        String json = getResourceFileAsString("webform/existingNoCorrespondent.json");

        ukviComplaintService.createComplaint(json);

        verify(complaintService).createComplaint(any(UKVIComplaintData.class), eq(complaintTypeData));
    }

}
