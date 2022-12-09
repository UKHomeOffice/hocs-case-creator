package uk.gov.digital.ho.hocs.domain.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.repositories.entities.EnumMappings;

import java.util.List;

@Slf4j
@Service
public class EnumMappingsRepository extends JsonConfigFileReader {

    private final List<EnumMappings.EnumMapping> enumMappings;

    public EnumMappingsRepository(ObjectMapper objectMapper) {
        super(objectMapper);

        enumMappings = readValueFromFile(new TypeReference<>() {});
    }

    public String getTextValueByNameAndLabel(String label, String name) {
        if (!enumMappings.isEmpty()) {
            for (EnumMappings.EnumMapping mapping : enumMappings) {
                if (mapping.getLabel().equals(label)) {
                    for (EnumMappings.Choice choice : mapping.getChoices()) {
                        if (choice.getName().equals(name)) {
                            return choice.getValue();
                        }
                    }
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
