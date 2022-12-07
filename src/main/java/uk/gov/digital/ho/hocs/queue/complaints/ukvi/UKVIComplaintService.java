package uk.gov.digital.ho.hocs.queue.complaints.ukvi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;
import uk.gov.digital.ho.hocs.queue.complaints.ComplaintService;

@Slf4j
@Service
public class UKVIComplaintService {

    private final ObjectMapper objectMapper;
    private final EnumMappingsRepository enumMappingsRepository;
    private final ComplaintService complaintService;
    private final ClientContext clientContext;
    private final UKVITypeData ukviTypeData;
    private final String user;
    private final String group;
    private final String team;

    @Autowired
    public UKVIComplaintService(ObjectMapper objectMapper,
                                EnumMappingsRepository enumMappingsRepository,
                                ComplaintService complaintService,
                                ClientContext clientContext,
                                UKVITypeData ukviTypeData,
                                @Value("${case.creator.identities.complaints.ukvi.user}") String user,
                                @Value("${case.creator.identities.complaints.ukvi.group}") String group,
                                @Value("${case.creator.identities.complaints.ukvi.team}") String team) {
        this.objectMapper = objectMapper;
        this.enumMappingsRepository = enumMappingsRepository;
        this.complaintService = complaintService;
        this.clientContext = clientContext;
        this.ukviTypeData = ukviTypeData;
        this.user = user;
        this.group = group;
        this.team = team;
    }

    public void createComplaint(String jsonBody, String messageId) {
        clientContext.setContext(user, group, team, messageId);
        complaintService.createComplaint(new UKVIComplaintData(jsonBody, objectMapper, enumMappingsRepository), ukviTypeData);
    }
}
