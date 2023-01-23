package uk.gov.digital.ho.hocs.domain.repositories.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class EnumMappings {

    @Getter
    public static class EnumMapping {
        private final String fieldName;
        private final Map<String, String> choices;

        @JsonCreator
        public EnumMapping(@JsonProperty("fieldName") String fieldName,
                           @JsonProperty("choices") Map<String, String> choices) {
            this.fieldName = fieldName;
            this.choices = choices;
        }
    }
}
