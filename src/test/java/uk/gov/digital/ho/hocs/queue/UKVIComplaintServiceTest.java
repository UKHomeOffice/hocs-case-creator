package uk.gov.digital.ho.hocs.queue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.client.ComplaintData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

@RunWith(MockitoJUnitRunner.class)
public class UKVIComplaintServiceTest {

    @Mock
    ComplaintService complaintService;

    @Test
    public void shouldCreateComplaint() throws Exception {
        UKVIComplaintService ukviComplaintService = new UKVIComplaintService(complaintService);
        String json = getResourceFileAsString("staffBehaviour.json");

        ukviComplaintService.createComplaint(json);

        verify(complaintService).createComplaint(any(ComplaintData.class), eq(UKVIComplaintService.CASE_TYPE));
    }

}