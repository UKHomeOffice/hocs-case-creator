package uk.gov.digital.ho.hocs.queue.ukvi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.queue.common.ComplaintService;

import java.io.IOException;

@Slf4j
@Service
public class UKVIComplaintService {
    public static final String CASE_TYPE = "COMP";
    private final ComplaintService complaintService;
    private final ClientContext clientContext;
    private final UKVITypeData ukviTypeData;
    private final String user;
    private final String group;
    private final String team;

    @Autowired
    public UKVIComplaintService(ComplaintService complaintService,
                                ClientContext clientContext,
                                UKVITypeData ukviTypeData,
                                @Value("${case.creator.ukvi-complaint.user}") String user,
                                @Value("${case.creator.ukvi-complaint.group}") String group,
                                @Value("${case.creator.ukvi-complaint.team}") String team) {
        this.complaintService = complaintService;
        this.clientContext = clientContext;
        this.ukviTypeData = ukviTypeData;
        this.user = user;
        this.group = group;
        this.team = team;
    }

    public void createComplaint(String jsonBody, String messageId) {
        clientContext.setContext(user, group, team, messageId);
        complaintService.createComplaint(new UKVIComplaintData(jsonBody), ukviTypeData);
    }
}
