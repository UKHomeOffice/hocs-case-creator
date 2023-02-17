package uk.gov.digital.ho.hocs.queue.complaints.ukvi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;
import uk.gov.digital.ho.hocs.queue.complaints.ComplaintService;

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

    public void createComplaint(String jsonBody) {
        complaintService.createComplaint(new UKVIComplaintData(jsonBody, objectMapper, enumMappingsRepository), ukviTypeData);
    }
}
