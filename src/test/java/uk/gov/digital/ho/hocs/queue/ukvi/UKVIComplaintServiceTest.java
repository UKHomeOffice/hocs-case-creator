package uk.gov.digital.ho.hocs.queue.ukvi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.queue.common.ComplaintService;
import uk.gov.digital.ho.hocs.queue.ukvi.UKVIComplaintData;
import uk.gov.digital.ho.hocs.queue.ukvi.UKVIComplaintService;
import uk.gov.digital.ho.hocs.queue.ukvi.UKVITypeData;

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
        UKVITypeData complaintTypeData = new UKVITypeData();
        UKVIComplaintService ukviComplaintService = new UKVIComplaintService(complaintService, clientContext, complaintTypeData, "user", "group");
        String json = getResourceFileAsString("staffBehaviour.json");

        ukviComplaintService.createComplaint(json, "messageId");

        verify(complaintService).createComplaint(any(UKVIComplaintData.class), eq(complaintTypeData));
    }

}