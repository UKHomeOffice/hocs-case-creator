package uk.gov.digital.ho.hocs.aws;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SQSQueuePrefix {
    private final String prefix;
}
