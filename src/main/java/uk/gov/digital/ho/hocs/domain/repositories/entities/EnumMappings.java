package uk.gov.digital.ho.hocs.domain.repositories.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

public class EnumMappings {

    @Getter
    public static class EnumMapping {
        private final String fieldName;
        private final List<Choice> choices;

        @JsonCreator
        public EnumMapping(@JsonProperty("fieldName") String fieldName,
                           @JsonProperty("choices") List<Choice> choices) {
            this.fieldName = fieldName;
            this.choices = choices;
        }
    }

    @Getter
    public static class Choice {

        private final String name;
        private final String value;

        @JsonCreator
        public Choice(@JsonProperty("name") String name,
                      @JsonProperty("value") String value) {
            this.name = name;
            this.value = value;
        }
    }
}
