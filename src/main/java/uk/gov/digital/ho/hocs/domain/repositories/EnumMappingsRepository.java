package uk.gov.digital.ho.hocs.domain.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.repositories.entities.EnumMappings;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EnumMappingsRepository extends JsonConfigFileReader {

    private final Map<String, List<EnumMappings.EnumMapping>> enumMappings;

    public EnumMappingsRepository(ObjectMapper objectMapper) {
        super(objectMapper);

        enumMappings = readValueFromFile(new TypeReference<>() {});
    }

    public String getLabelByTypeAndName(String type, String name) {
        List<EnumMappings.EnumMapping> fieldMappings = enumMappings.get("fields");
        if (!fieldMappings.isEmpty()) {
            for (EnumMappings.EnumMapping mapping : fieldMappings) {
                if (mapping.getType().equals(type)) {
                    for (EnumMappings.Choice choice : mapping.getChoices()) {
                        if (choice.getName().equals(name)) {
                            return choice.getLabel();
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
