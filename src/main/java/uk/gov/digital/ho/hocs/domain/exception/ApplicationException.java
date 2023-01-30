package uk.gov.digital.ho.hocs.domain.exception;

import uk.gov.digital.ho.hocs.application.LogEvent;

public interface ApplicationException {

    class EntityCreationException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public EntityCreationException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public EntityCreationException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}
    }
}
