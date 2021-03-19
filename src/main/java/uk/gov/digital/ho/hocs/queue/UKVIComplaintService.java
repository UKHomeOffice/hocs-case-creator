package uk.gov.digital.ho.hocs.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.queue.data.UKVIComplaintData;

@Slf4j
@Service
public class UKVIComplaintService {
    public static final String CASE_TYPE = "COMP";
    private final ComplaintService complaintService;
    private final ClientContext clientContext;

    @Value("${case.creator.ukvi-complaint.user}")
    private String user;
    @Value("${case.creator.ukvi-complaint.group}")
    private String group;

    @Autowired
    public UKVIComplaintService(ComplaintService complaintService, ClientContext clientContext) {
        this.complaintService = complaintService;
        this.clientContext = clientContext;
    }

    public void createComplaint(String jsonBody, String messageId) {
        clientContext.setContext(user, group, messageId);
        complaintService.createComplaint(new UKVIComplaintData(jsonBody), CASE_TYPE);
    }
}
