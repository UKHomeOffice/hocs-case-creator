package uk.gov.digital.ho.hocs.domain.exceptions;

import uk.gov.digital.ho.hocs.application.LogEvent;

public interface ApplicationExceptions {

     class ConfigFileReadException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ConfigFileReadException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }
}
