package uk.gov.digital.ho.hocs.domain.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static uk.gov.digital.ho.hocs.application.LogEvent.EVENT;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler(ApplicationExceptions.TooManyMessagesException.class)
    public ResponseEntity<String> handle(ApplicationExceptions.TooManyMessagesException e) {
        log.error("ApplicationExceptions.TooManyMessagesException: {}, Event: {}", e.getMessage(),
            value(EVENT, e.getEvent()));
        return new ResponseEntity<>(e.getMessage(), SERVICE_UNAVAILABLE);
    }

}
