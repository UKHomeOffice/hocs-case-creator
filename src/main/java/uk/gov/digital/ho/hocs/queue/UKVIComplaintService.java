package uk.gov.digital.ho.hocs.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.client.ComplaintData;

@Slf4j
@Service
public class UKVIComplaintService {
    public static final String CASE_TYPE = "COMP";
    private final ComplaintService complaintService;

    @Autowired
    public UKVIComplaintService(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    public void createComplaint(String jsonBody) throws Exception {
        complaintService.createComplaint(new ComplaintData(jsonBody), CASE_TYPE);
    }
}
