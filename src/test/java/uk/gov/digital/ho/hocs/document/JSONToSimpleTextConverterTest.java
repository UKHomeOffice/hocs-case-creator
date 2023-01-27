package uk.gov.digital.ho.hocs.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
public class JSONToSimpleTextConverterTest {

    @SpyBean
    private ObjectMapper objectMapper;

    @SpyBean
    private EnumMappingsRepository enumMappingsRepository;

    @Test
    public void shouldBuildFormattedDocument() throws IOException {
        String json = getResourceFileAsString("staffBehaviour.json");
        String expectedText = getResourceFileAsString("staffBehaviourTextConverted.txt");

        doReturn("").when(enumMappingsRepository).getTextValueByNameAndFieldName("creationDate", "2020-10-03");
        doReturn("Staff behaviour").when(enumMappingsRepository).getTextValueByNameAndFieldName("complaintType", "POOR_STAFF_BEHAVIOUR");
        doReturn("IHS reference").when(enumMappingsRepository).getTextValueByNameAndFieldName("referenceType", "IHS_REF");
        doReturn("").when(enumMappingsRepository).getTextValueByNameAndFieldName("reference", "ABC12345");
        doReturn("Agent").when(enumMappingsRepository).getTextValueByNameAndFieldName("applicantType", "AGENT");
        doReturn("").when(enumMappingsRepository).getTextValueByNameAndFieldName("applicantDob", "1989-08-23");
        doReturn("Relative").when(enumMappingsRepository).getTextValueByNameAndFieldName("agentType", "RELATIVE");
        doReturn("Face to face").when(enumMappingsRepository).getTextValueByNameAndFieldName("experienceType", "FACE_TO_FACE");
        doReturn("VAC (visa application centre)").when(enumMappingsRepository).getTextValueByNameAndFieldName("centreType", "VAC");

        JSONToSimpleTextConverter complaintData = new JSONToSimpleTextConverter(json, objectMapper, enumMappingsRepository);
        assertEquals(expectedText, complaintData.getConvertedOutput());
    }
}