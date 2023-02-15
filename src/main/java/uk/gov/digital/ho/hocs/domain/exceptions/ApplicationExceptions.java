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

    class DocumentCreationException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public DocumentCreationException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class CaseCreationException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public CaseCreationException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class CaseCorrespondentCreationException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public CaseCorrespondentCreationException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class CaseStageRetrievalException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public CaseStageRetrievalException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class CaseUserUpdateException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public CaseUserUpdateException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class CaseTeamUpdateException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public CaseTeamUpdateException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class InvalidMessageTypeException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public InvalidMessageTypeException(String msg, LogEvent event, Object... args) {
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
