package uk.gov.digital.ho.hocs.domain.document;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;

import java.io.IOException;

@Slf4j
public class JSONToSimpleTextConverter {
    public static final String NEW_LINE_STR = "\n            ";
    public static final int WRAP_LENGTH = 60;
    public static final boolean WRAP_LONG_WORDS = false;
    private final String inputJson;
    private final StringBuilder convertedOutput = new StringBuilder();
    private final ObjectMapper objectMapper;
    private final EnumMappingsRepository enumMappingsRepository;

    public JSONToSimpleTextConverter(String inputJson, ObjectMapper objectMapper, EnumMappingsRepository enumMappingsRepository) throws IOException {
        this.inputJson = inputJson;
        this.objectMapper = objectMapper;
        this.enumMappingsRepository = enumMappingsRepository;
        convertedOutput.append("\n\n");
        convert();
    }

    public String getConvertedOutput() {
        return convertedOutput.toString();
    }

    private void convert() throws IOException {
        JsonNode rootNode = objectMapper.readTree(inputJson);
        traverse(rootNode, 1);
    }

    private void traverse(JsonNode node, int level) {
        node.fieldNames().forEachRemaining((String fieldName) -> {
            JsonNode childNode = node.get(fieldName);
            buildString(childNode, fieldName, level);
            //for nested object
            if (isTraversable(childNode)) {
                traverse(childNode, level + 1);
            }
        });
    }

    private static boolean isTraversable(JsonNode node) {
        return node.getNodeType() == JsonNodeType.OBJECT ||
                node.getNodeType() == JsonNodeType.ARRAY;
    }

    private void buildString(JsonNode node, String keyName, int level) {
        if (isTraversable(node)) {
            convertedOutput.append(String.format("%n%" + (level * 4 - 3) + "s %s%n", "", fromJavaIdentifierToDisplayableString(keyName)));
        } else {
            String textValue = sanitizeInput(node.textValue());

            if (textValue.equals(textValue.toUpperCase())) {
                String label = enumMappingsRepository.getTextValueByNameAndFieldName(keyName, textValue);
                if (label != null && !label.isEmpty()) {
                    textValue = label;
                }
            }
            if (keyName.equals("complaintText")) {
                textValue = textValue.replaceAll("[\\n]", NEW_LINE_STR);
                textValue = WordUtils.wrap(NEW_LINE_STR + textValue, WRAP_LENGTH, NEW_LINE_STR, WRAP_LONG_WORDS);
                convertedOutput.append(String.format("%" + (level * 4 - 3) + "s %s :%s%n", "", fromJavaIdentifierToDisplayableString(keyName), textValue));
                return;
            }
            convertedOutput.append(String.format("%" + (level * 4 - 3) + "s %s : %s%n", "", fromJavaIdentifierToDisplayableString(keyName), textValue));
        }
    }

    private String[] splitByCapitalLetters(String input) {
        return input.split("(?=\\p{Upper})");
    }

    private String fromJavaIdentifierToDisplayableString(String input) {
        StringBuilder splitString = new StringBuilder();
        for (String s : splitByCapitalLetters(input)) {
            splitString.append(s);
            splitString.append(" ");
        }
        String displayable = splitString.toString().trim();
        if (Character.isLowerCase(displayable.charAt(0))) {
            displayable = Character.toUpperCase(displayable.charAt(0)) + displayable.substring(1);
        }
        return displayable;
    }

    private String sanitizeInput(String input) {
        // Clean html tags but allow special characters
        Document.OutputSettings outputSettings = new Document.OutputSettings();
        return Jsoup.clean(input, "", Safelist.none(), outputSettings.prettyPrint(false))
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }

}
