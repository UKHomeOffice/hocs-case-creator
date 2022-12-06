package uk.gov.digital.ho.hocs.domain.repositories.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

public class EnumMappings {

    private static List<EnumMapping> fields;

    @JsonCreator
    public EnumMappings(@JsonProperty("fields") List<EnumMapping> fields) {
        this.fields = fields;
    }
    
    public List<EnumMapping> getMappingsByType(String type) {
        List<EnumMapping> typeMappings = null;
        
        for (EnumMapping mapping : fields) {
            if (mapping.type.equals(type)) {
                typeMappings.add(mapping);
            }
        }
        return typeMappings;
    }

    @Getter
    public static class EnumMapping {
        private final String type;
        private final List<Choice> choices;

        @JsonCreator
        public EnumMapping(@JsonProperty("type") String type,
                           @JsonProperty("choices") List<Choice> choices) {
            this.type = type;
            this.choices = choices;
        }
    }

    @Getter
    public static class Choice {

        private final String name;
        private final String label;

        @JsonCreator
        public Choice(@JsonProperty("name") String name,
                      @JsonProperty("label") String label) {
            this.name = name;
            this.label = label;
        }
    }
}
