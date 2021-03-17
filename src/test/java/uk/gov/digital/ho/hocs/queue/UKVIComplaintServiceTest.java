package uk.gov.digital.ho.hocs.queue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.client.UKVIComplaintData;

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

    @Test
    public void shouldCreateComplaint() {
        UKVIComplaintService ukviComplaintService = new UKVIComplaintService(complaintService, clientContext);
        String json = getResourceFileAsString("staffBehaviour.json");

        ukviComplaintService.createComplaint(json, "messageId");

        verify(complaintService).createComplaint(any(UKVIComplaintData.class), eq(UKVIComplaintService.CASE_TYPE));
    }

}