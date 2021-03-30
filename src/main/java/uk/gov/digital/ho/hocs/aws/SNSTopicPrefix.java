package uk.gov.digital.ho.hocs.aws;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SNSTopicPrefix {
    private final String prefix;
}
