package uk.gov.digital.ho.hocs.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UKVIComplaintService {
    public void createComplaint(String body) {
        log.info(body);
    }
}
