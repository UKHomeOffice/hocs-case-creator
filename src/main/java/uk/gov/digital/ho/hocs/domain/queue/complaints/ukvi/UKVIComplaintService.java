package uk.gov.digital.ho.hocs.domain.queue.complaints.ukvi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.queue.complaints.ComplaintService;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;

@Slf4j
@Service
public class UKVIComplaintService {

    private final ObjectMapper objectMapper;
    private final EnumMappingsRepository enumMappingsRepository;
    private final ComplaintService complaintService;
    private final UKVITypeData ukviTypeData;

    @Autowired
    public UKVIComplaintService(ObjectMapper objectMapper,
                                EnumMappingsRepository enumMappingsRepository,
                                ComplaintService complaintService,
                                UKVITypeData ukviTypeData) {
        this.objectMapper = objectMapper;
        this.enumMappingsRepository = enumMappingsRepository;
        this.complaintService = complaintService;
        this.ukviTypeData = ukviTypeData;
    }

    public void createComplaint(Message message) {
        complaintService.createComplaint(message.id(), new UKVIComplaintData(message.message(), objectMapper, enumMappingsRepository), ukviTypeData);
    }
}
