package uk.gov.digital.ho.hocs.domain.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.repositories.entities.EnumMappings;

import java.util.List;

@Service
public class EnumMappingsRepository extends JsonConfigFileReader {

    private final List<EnumMappings.EnumMapping> enumMappings;

    public EnumMappingsRepository(ObjectMapper objectMapper) {
        super(objectMapper);

        enumMappings = readValueFromFile(new TypeReference<>() {});
    }

    public String getTextValueByNameAndFieldName(String fieldName, String name) {
        if (!enumMappings.isEmpty()) {
            for (EnumMappings.EnumMapping mapping : enumMappings) {
                if (mapping.getFieldName().equals(fieldName)) {
                    return mapping.getChoices().get(name);
                }
            }
        }
        return "";
    }

    @Override
    String getFileName() {
        return "enum-mappings";
    }
}
