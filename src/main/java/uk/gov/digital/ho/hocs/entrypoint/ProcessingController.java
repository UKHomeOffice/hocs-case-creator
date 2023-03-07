package uk.gov.digital.ho.hocs.entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.domain.service.ProcessingService;
import uk.gov.digital.ho.hocs.entrypoint.model.ProcessingRequest;

@RestController
@Slf4j
public class ProcessingController {

    private final ProcessingService processingService;

    public ProcessingController(ProcessingService processingService) {
        this.processingService = processingService;
    }

    @PostMapping("/process")
    public ResponseEntity<Void> processMessages(@RequestBody ProcessingRequest processingRequest) {
        log.info("Processing {} messages from {} to {}.", processingRequest.getMaxMessages(), processingRequest.getFrom(), processingRequest.getTo());
        processingService.retrieveAndProcessMessages(processingRequest.getMaxMessages(),
                processingRequest.getFrom(),
                processingRequest.getTo());
        log.info("Finished processing messages.");
        return ResponseEntity.ok().build();
    }

}
