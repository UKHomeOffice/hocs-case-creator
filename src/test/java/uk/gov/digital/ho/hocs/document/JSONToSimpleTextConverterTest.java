package uk.gov.digital.ho.hocs.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

@RunWith(MockitoJUnitRunner.class)
public class JSONToSimpleTextConverterTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EnumMappingsRepository enumMappingsRepository;

    @Test
    public void shouldBuildFormattedDocument() throws IOException {
        String json = getResourceFileAsString("staffBehaviour.json");
        String expectedText = getResourceFileAsString("staffBehaviourTextConverted.txt");
        when(enumMappingsRepository.getTextValueByNameAndLabel("creationDate", "2020-10-03")).thenReturn("");
        when(enumMappingsRepository.getTextValueByNameAndLabel("complaintType", "POOR_STAFF_BEHAVIOUR")).thenReturn("Staff behaviour");
        when(enumMappingsRepository.getTextValueByNameAndLabel("referenceType", "IHS_REF")).thenReturn("");
        when(enumMappingsRepository.getTextValueByNameAndLabel("reference", "ABC12345")).thenReturn("");
        when(enumMappingsRepository.getTextValueByNameAndLabel("applicantType", "AGENT")).thenReturn("Agent");
        when(enumMappingsRepository.getTextValueByNameAndLabel("applicantDob", "1989-08-23")).thenReturn("");
        when(enumMappingsRepository.getTextValueByNameAndLabel("agentType", "RELATIVE")).thenReturn("Relative");
        when(enumMappingsRepository.getTextValueByNameAndLabel("experienceType", "FACE_TO_FACE")).thenReturn("Face to face");
        when(enumMappingsRepository.getTextValueByNameAndLabel("centreType", "VAC")).thenReturn("");

        JSONToSimpleTextConverter complaintData = new JSONToSimpleTextConverter(json, objectMapper, enumMappingsRepository);
        assertEquals(expectedText, complaintData.getConvertedOutput());
    }
}