package uk.gov.digital.ho.hocs.queue;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UKVIComplaintService {
    public static final String CASE_TYPE = "COMP";

    public void createComplaint(String jsonBody) throws Exception {
        log.info("Complaint : {}", (String) JsonPath.read(jsonBody, "$.complaint.complaintType"));
    }
}
